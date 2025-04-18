/*
 * Copyright (c) 2023-2024 lax1dude. All Rights Reserved.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package net.lax1dude.eaglercraft.v1_8.sp.internal;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.dom.events.ErrorEvent;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.HTMLScriptElement;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.workers.Worker;

import net.lax1dude.eaglercraft.v1_8.internal.IPCPacketData;
import net.lax1dude.eaglercraft.v1_8.internal.PlatformRuntime;
import net.lax1dude.eaglercraft.v1_8.internal.PlatformWebRTC;
import net.lax1dude.eaglercraft.v1_8.internal.teavm.ClientMain;
import net.lax1dude.eaglercraft.v1_8.internal.teavm.TeaVMBlobURLManager;
import net.lax1dude.eaglercraft.v1_8.internal.teavm.TeaVMClientConfigAdapter;
import net.lax1dude.eaglercraft.v1_8.internal.teavm.TeaVMUtils;
import net.lax1dude.eaglercraft.v1_8.log4j.LogManager;
import net.lax1dude.eaglercraft.v1_8.log4j.Logger;
import net.lax1dude.eaglercraft.v1_8.sp.server.internal.teavm.SingleThreadWorker;

public class ClientPlatformSingleplayer {

	private static final Logger logger = LogManager.getLogger("ClientPlatformSingleplayer");

	private static final LinkedList<IPCPacketData> messageQueue = new LinkedList<>();

	@JSBody(params = {}, script = "return (typeof eaglercraftXClientScriptElement !== \"undefined\") ? eaglercraftXClientScriptElement : null;")
	private static native JSObject loadIntegratedServerSourceOverride();

	@JSBody(params = {}, script = "return (typeof eaglercraftXClientScriptURL === \"string\") ? eaglercraftXClientScriptURL : null;")
	private static native String loadIntegratedServerSourceOverrideURL();

	@JSBody(params = {}, script = "try{throw new Error();}catch(ex){return ex.stack||null;}return null;")
	private static native String loadIntegratedServerSourceStack();

	@JSBody(params = { "csc" }, script = "if(typeof csc.src === \"string\" && csc.src.length > 0) return csc.src; else return null;")
	private static native String loadIntegratedServerSourceURL(JSObject scriptTag);

	@JSBody(params = { "csc", "tail" }, script = "var cscText = csc.text;"
			+ "if(typeof cscText === \"string\" && cscText.length > 0) return new Blob([cscText, tail], { type: \"text/javascript;charset=utf8\" });"
			+ "else return null;")
	private static native JSObject loadIntegratedServerSourceInline(JSObject scriptTag, String tail);

	@JSBody(params = { "csc" }, script = "var cscText = csc.text;"
			+ "if(typeof cscText === \"string\" && cscText.length > 0) return cscText;"
			+ "else return null;")
	private static native String loadIntegratedServerSourceInlineStr(JSObject scriptTag);

	private static String integratedServerSource = null;
	private static String integratedServerSourceOriginalURL = null;
	private static boolean serverSourceLoaded = false;
	private static boolean isSingleThreadMode = false;

	private static Worker workerObj = null;

	@JSFunctor
	private static interface WorkerBinaryPacketHandler extends JSObject {
		public void onMessage(String channel, ArrayBuffer buf);
	}

	@JSBody(params = { "w", "wb" }, script = "w.addEventListener(\"message\", function(o) { wb(o.data.ch, o.data.dat); });")
	private static native void registerPacketHandler(Worker w, WorkerBinaryPacketHandler wb);

	@JSBody(params = { "w", "ch", "dat" }, script = "w.postMessage({ ch: ch, dat : dat });")
	private static native void sendWorkerPacket(Worker w, String channel, ArrayBuffer arr);

	@JSBody(params = { "w", "workerArgs" }, script = "w.postMessage({ msg : workerArgs });")
	private static native void sendWorkerStartPacket(Worker w, String workerArgs);

	private static class WorkerBinaryPacketHandlerImpl implements WorkerBinaryPacketHandler {
		
		public void onMessage(String channel, ArrayBuffer buf) {
			if(channel == null) {
				logger.error("Recieved IPC packet with null channel");
				return;
			}
			
			if(buf == null) {
				logger.error("Recieved IPC packet with null buffer");
				return;
			}
			
			if(PlatformWebRTC.serverLANPeerPassIPC(channel, buf)) {
				return;
			}
			
			synchronized(messageQueue) {
				messageQueue.add(new IPCPacketData(channel, TeaVMUtils.wrapByteArrayBuffer(buf)));
			}
		}
		
	}

	@JSBody(params = { "blobObj" }, script = "return URL.createObjectURL(blobObj);")
	private static native String createWorkerScriptURL(JSObject blobObj);

	@JSBody(params = { "cscText", "tail" }, script = "return new Blob([cscText, tail], { type: \"text/javascript;charset=utf8\" });")
	private static native JSObject createBlobObj(ArrayBuffer buf, String tail);

	private static final String workerBootstrapCode = "\n\nmain([\"_worker_process_\"]);";

	private static JSObject loadIntegratedServerSource() {
		String str = loadIntegratedServerSourceOverrideURL();
		if(str != null) {
			ArrayBuffer buf = PlatformRuntime.downloadRemoteURI(str, true);
			if(buf != null) {
				integratedServerSourceOriginalURL = str;
				logger.info("Using integrated server at: {}", truncateURL(str));
				return createBlobObj(buf, workerBootstrapCode);
			}else {
				logger.error("Failed to load integrated server: {}", truncateURL(str));
			}
		}
		JSObject el = loadIntegratedServerSourceOverride();
		if(el != null) {
			String url = loadIntegratedServerSourceURL(el);
			if(url == null) {
				el = loadIntegratedServerSourceInline(el, workerBootstrapCode);
				if(el != null) {
					integratedServerSourceOriginalURL = "inline script tag";
					logger.info("Loading integrated server from inline script tag");
					return el;
				}
			}else {
				ArrayBuffer buf = PlatformRuntime.downloadRemoteURI(url, true);
				if(buf != null) {
					integratedServerSourceOriginalURL = url;
					logger.info("Using integrated server from script tag src: {}", truncateURL(url));
					return createBlobObj(buf, workerBootstrapCode);
				}else {
					logger.error("Failed to load integrated server from script tag src: {}", truncateURL(url));
				}
			}
		}
		str = TeaVMUtils.tryResolveClassesSource();
		if(str != null) {
			ArrayBuffer buf = PlatformRuntime.downloadRemoteURI(str, true);
			if(buf != null) {
				integratedServerSourceOriginalURL = str;
				logger.info("Using integrated server from script src: {}", truncateURL(str));
				return createBlobObj(buf, workerBootstrapCode);
			}else {
				logger.error("Failed to load integrated server from script src: {}", truncateURL(str));
			}
		}
		HTMLScriptElement sc = TeaVMUtils.tryResolveClassesSourceInline();
		if(sc != null) {
			el = loadIntegratedServerSourceInline(sc, workerBootstrapCode);
			if(el != null) {
				integratedServerSourceOriginalURL = "inline script tag (client guess)";
				logger.info("Loading integrated server from (likely) inline script tag");
				return el;
			}
		}
		logger.info("Could not resolve the location of client's classes.js!");
		logger.info("Make sure client's classes.js is linked/embedded in a dedicated <script> tag");
		logger.info("Define \"window.eaglercraftXClientScriptElement\" or \"window.eaglercraftXClientScriptURL\" to force");
		return null;
	}

	private static String truncateURL(String url) {
		if(url == null) return null;
		if(url.length() > 256) {
			url = url.substring(0, 254) + "...";
		}
		return url;
	}

	private static String createIntegratedServerWorkerURL() {
		JSObject blobObj = loadIntegratedServerSource();
		if(blobObj == null) {
			return null;
		}
		return TeaVMBlobURLManager.registerNewURLBlob(blobObj).toExternalForm();
	}

	public static byte[] getIntegratedServerSourceTeaVM() {
		String str = loadIntegratedServerSourceOverrideURL();
		if(str != null) {
			ArrayBuffer buf = PlatformRuntime.downloadRemoteURI(str, true);
			if(buf != null) {
				return TeaVMUtils.wrapByteArrayBuffer(buf);
			}
		}
		JSObject el = loadIntegratedServerSourceOverride();
		if(el != null) {
			String url = loadIntegratedServerSourceURL(el);
			if(url == null) {
				str = loadIntegratedServerSourceInlineStr(el);
				if(str != null) {
					return str.getBytes(StandardCharsets.UTF_8);
				}
			}else {
				ArrayBuffer buf = PlatformRuntime.downloadRemoteURI(url, true);
				if(buf != null) {
					return TeaVMUtils.wrapByteArrayBuffer(buf);
				}
			}
		}
		str = TeaVMUtils.tryResolveClassesSource();
		if(str != null) {
			ArrayBuffer buf = PlatformRuntime.downloadRemoteURI(str, true);
			if(buf != null) {
				return TeaVMUtils.wrapByteArrayBuffer(buf);
			}
		}
		HTMLScriptElement sc = TeaVMUtils.tryResolveClassesSourceInline();
		if(sc != null) {
			return sc.getText().getBytes(StandardCharsets.UTF_8);
		}
		return null;
	}

	public static String getLoadedWorkerURLTeaVM() {
		return (serverSourceLoaded && workerObj != null) ? integratedServerSource : null;
	}

	public static String getLoadedWorkerSourceURLTeaVM() {
		return (serverSourceLoaded && workerObj != null) ? integratedServerSourceOriginalURL : null;
	}

	public static void startIntegratedServer(boolean singleThreadMode) {
		singleThreadMode |= ((TeaVMClientConfigAdapter)PlatformRuntime.getClientConfigAdapter()).isSingleThreadModeTeaVM();
		if(singleThreadMode) {
			if(!isSingleThreadMode) {
				SingleThreadWorker.singleThreadStartup((pkt) -> {
					synchronized(messageQueue) {
						messageQueue.add(pkt);
					}
				});
				isSingleThreadMode = true;
			}
		}else {
			if(!serverSourceLoaded) {
				integratedServerSource = createIntegratedServerWorkerURL();
				serverSourceLoaded = true;
			}
			
			if(integratedServerSource == null) {
				logger.error("Could not resolve the location of client's classes.js! Make sure client's classes.js is linked/embedded in a dedicated <script> tag. Define \"window.eaglercraftXClientScriptElement\" or \"window.eaglercraftXClientScriptURL\" to force");
				logger.error("Falling back to single thread mode...");
				startIntegratedServer(true);
				return;
			}
			
			workerObj = Worker.create(integratedServerSource);
			workerObj.addEventListener("error", new EventListener<ErrorEvent>() {
				@Override
				public void handleEvent(ErrorEvent evt) {
					logger.error("Worker Error: {}", evt.getError());
					PlatformRuntime.printNativeExceptionToConsoleTeaVM(evt);
				}
			});
			registerPacketHandler(workerObj, new WorkerBinaryPacketHandlerImpl());
			sendWorkerStartPacket(workerObj, PlatformRuntime.getClientConfigAdapter().getIntegratedServerOpts().toString());
		}
	}

	public static void sendPacket(IPCPacketData packet) {
		if(isSingleThreadMode) {
			SingleThreadWorker.sendPacketToWorker(packet);
		}else {
			sendPacketTeaVM(packet.channel, TeaVMUtils.unwrapArrayBuffer(packet.contents));
		}
	}

	public static void sendPacketTeaVM(String channel, ArrayBuffer packet) {
		if(isSingleThreadMode) {
			SingleThreadWorker.sendPacketToWorker(new IPCPacketData(channel, TeaVMUtils.wrapByteArrayBuffer(packet)));
		}else {
			if(workerObj != null) {
				sendWorkerPacket(workerObj, channel, packet);
			}
		}
	}

	public static List<IPCPacketData> recieveAllPacket() {
		synchronized(messageQueue) {
			if(messageQueue.size() == 0) {
				return null;
			}else {
				List<IPCPacketData> ret = new ArrayList<>(messageQueue);
				messageQueue.clear();
				return ret;
			}
		}
	}

	public static boolean canKillWorker() {
		return !isSingleThreadMode;
	}

	public static void killWorker() {
		if(workerObj != null) {
			workerObj.terminate();
			workerObj = null;
		}
	}

	public static boolean isRunningSingleThreadMode() {
		return isSingleThreadMode;
	}

	public static boolean isSingleThreadModeSupported() {
		return true;
	}

	public static void updateSingleThreadMode() {
		if(isSingleThreadMode) {
			SingleThreadWorker.singleThreadUpdate();
		}
	}

	public static void showCrashReportOverlay(String report, int x, int y, int w, int h) {
		ClientMain.showIntegratedServerCrashReportOverlay(report, x, y, w, h);
	}

	public static void hideCrashReportOverlay() {
		ClientMain.hideIntegratedServerCrashReportOverlay();
	}

}
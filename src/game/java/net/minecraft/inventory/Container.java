package net.minecraft.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;

/**+
 * This portion of EaglercraftX contains deobfuscated Minecraft 1.8 source code.
 * 
 * Minecraft 1.8.8 bytecode is (c) 2015 Mojang AB. "Do not distribute!"
 * Mod Coder Pack v9.18 deobfuscation configs are (c) Copyright by the MCP Team
 * 
 * EaglercraftX 1.8 patch files (c) 2022-2025 lax1dude, ayunami2000. All Rights Reserved.
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
public abstract class Container {
	/**+
	 * the list of all items(stacks) for the corresponding slot
	 */
	public List<ItemStack> inventoryItemStacks = Lists.newArrayList();
	/**+
	 * the list of all slots in the inventory
	 */
	public List<Slot> inventorySlots = Lists.newArrayList();
	public int windowId;
	private short transactionID;
	/**+
	 * The current drag mode (0 : evenly split, 1 : one item by
	 * slot, 2 : not used ?)
	 */
	private int dragMode = -1;
	private int dragEvent;
	/**+
	 * The list of slots where the itemstack holds will be
	 * distributed
	 */
	private final Set<Slot> dragSlots = Sets.newHashSet();
	/**+
	 * list of all people that need to be notified when this
	 * craftinventory changes
	 */
	protected List<ICrafting> crafters = Lists.newArrayList();
	private Set<EntityPlayer> playerList = Sets.newHashSet();

	/**+
	 * Adds an item slot to this container
	 */
	protected Slot addSlotToContainer(Slot slotIn) {
		slotIn.slotNumber = this.inventorySlots.size();
		this.inventorySlots.add(slotIn);
		this.inventoryItemStacks.add((ItemStack) null);
		return slotIn;
	}

	public void onCraftGuiOpened(ICrafting listener) {
		if (this.crafters.contains(listener)) {
			throw new IllegalArgumentException("Listener already listening");
		} else {
			this.crafters.add(listener);
			listener.updateCraftingInventory(this, this.getInventory());
			this.detectAndSendChanges();
		}
	}

	/**+
	 * Remove the given Listener. Method name is for legacy.
	 */
	public void removeCraftingFromCrafters(ICrafting listeners) {
		this.crafters.remove(listeners);
	}

	/**+
	 * returns a list if itemStacks, for each slot.
	 */
	public List<ItemStack> getInventory() {
		ArrayList arraylist = Lists.newArrayList();

		for (int i = 0; i < this.inventorySlots.size(); ++i) {
			arraylist.add(((Slot) this.inventorySlots.get(i)).getStack());
		}

		return arraylist;
	}

	/**+
	 * Looks for changes made in the container, sends them to every
	 * listener.
	 */
	public void detectAndSendChanges() {
		for (int i = 0; i < this.inventorySlots.size(); ++i) {
			ItemStack itemstack = ((Slot) this.inventorySlots.get(i)).getStack();
			ItemStack itemstack1 = (ItemStack) this.inventoryItemStacks.get(i);
			if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
				itemstack1 = itemstack == null ? null : itemstack.copy();
				this.inventoryItemStacks.set(i, itemstack1);

				for (int j = 0; j < this.crafters.size(); ++j) {
					((ICrafting) this.crafters.get(j)).sendSlotContents(this, i, itemstack1);
				}
			}
		}

	}

	/**+
	 * Handles the given Button-click on the server, currently only
	 * used by enchanting. Name is for legacy.
	 */
	public boolean enchantItem(EntityPlayer playerIn, int id) {
		return false;
	}

	public Slot getSlotFromInventory(IInventory inv, int slotIn) {
		for (int i = 0; i < this.inventorySlots.size(); ++i) {
			Slot slot = (Slot) this.inventorySlots.get(i);
			if (slot.isHere(inv, slotIn)) {
				return slot;
			}
		}

		return null;
	}

	public Slot getSlot(int slotId) {
		return (Slot) this.inventorySlots.get(slotId);
	}

	/**+
	 * Take a stack from the specified inventory slot.
	 */
	public ItemStack transferStackInSlot(EntityPlayer var1, int i) {
		Slot slot = (Slot) this.inventorySlots.get(i);
		return slot != null ? slot.getStack() : null;
	}

	/**+
	 * Handles slot click.
	 */
	public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer playerIn) {
		ItemStack itemstack = null;
		InventoryPlayer inventoryplayer = playerIn.inventory;
		if (mode == 5) {
			int i = this.dragEvent;
			this.dragEvent = getDragEvent(clickedButton);
			if ((i != 1 || this.dragEvent != 2) && i != this.dragEvent) {
				this.resetDrag();
			} else if (inventoryplayer.getItemStack() == null) {
				this.resetDrag();
			} else if (this.dragEvent == 0) {
				this.dragMode = extractDragMode(clickedButton);
				if (isValidDragMode(this.dragMode, playerIn)) {
					this.dragEvent = 1;
					this.dragSlots.clear();
				} else {
					this.resetDrag();
				}
			} else if (this.dragEvent == 1) {
				Slot slot = (Slot) this.inventorySlots.get(slotId);
				if (slot != null && canAddItemToSlot(slot, inventoryplayer.getItemStack(), true)
						&& slot.isItemValid(inventoryplayer.getItemStack())
						&& inventoryplayer.getItemStack().stackSize > this.dragSlots.size()
						&& this.canDragIntoSlot(slot)) {
					this.dragSlots.add(slot);
				}
			} else if (this.dragEvent == 2) {
				if (!this.dragSlots.isEmpty()) {
					ItemStack itemstack3 = inventoryplayer.getItemStack().copy();
					int j = inventoryplayer.getItemStack().stackSize;

					for (Slot slot1 : this.dragSlots) {
						if (slot1 != null && canAddItemToSlot(slot1, inventoryplayer.getItemStack(), true)
								&& slot1.isItemValid(inventoryplayer.getItemStack())
								&& inventoryplayer.getItemStack().stackSize >= this.dragSlots.size()
								&& this.canDragIntoSlot(slot1)) {
							ItemStack itemstack1 = itemstack3.copy();
							int k = slot1.getHasStack() ? slot1.getStack().stackSize : 0;
							computeStackSize(this.dragSlots, this.dragMode, itemstack1, k);
							if (itemstack1.stackSize > itemstack1.getMaxStackSize()) {
								itemstack1.stackSize = itemstack1.getMaxStackSize();
							}

							if (itemstack1.stackSize > slot1.getItemStackLimit(itemstack1)) {
								itemstack1.stackSize = slot1.getItemStackLimit(itemstack1);
							}

							j -= itemstack1.stackSize - k;
							slot1.putStack(itemstack1);
						}
					}

					itemstack3.stackSize = j;
					if (itemstack3.stackSize <= 0) {
						itemstack3 = null;
					}

					inventoryplayer.setItemStack(itemstack3);
				}

				this.resetDrag();
			} else {
				this.resetDrag();
			}
		} else if (this.dragEvent != 0) {
			this.resetDrag();
		} else if ((mode == 0 || mode == 1) && (clickedButton == 0 || clickedButton == 1)) {
			if (slotId == -999) {
				if (inventoryplayer.getItemStack() != null) {
					if (clickedButton == 0) {
						playerIn.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack(), true);
						inventoryplayer.setItemStack((ItemStack) null);
					}

					if (clickedButton == 1) {
						playerIn.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack().splitStack(1), true);
						if (inventoryplayer.getItemStack().stackSize == 0) {
							inventoryplayer.setItemStack((ItemStack) null);
						}
					}
				}
			} else if (mode == 1) {
				if (slotId < 0) {
					return null;
				}

				Slot slot6 = (Slot) this.inventorySlots.get(slotId);
				if (slot6 != null && slot6.canTakeStack(playerIn)) {
					ItemStack itemstack8 = this.transferStackInSlot(playerIn, slotId);
					if (itemstack8 != null) {
						Item item = itemstack8.getItem();
						itemstack = itemstack8.copy();
						if (slot6.getStack() != null && slot6.getStack().getItem() == item) {
							this.retrySlotClick(slotId, clickedButton, true, playerIn);
						}
					}
				}
			} else {
				if (slotId < 0) {
					return null;
				}

				Slot slot7 = (Slot) this.inventorySlots.get(slotId);
				if (slot7 != null) {
					ItemStack itemstack9 = slot7.getStack();
					ItemStack itemstack10 = inventoryplayer.getItemStack();
					if (itemstack9 != null) {
						itemstack = itemstack9.copy();
					}

					if (itemstack9 == null) {
						if (itemstack10 != null && slot7.isItemValid(itemstack10)) {
							int k2 = clickedButton == 0 ? itemstack10.stackSize : 1;
							if (k2 > slot7.getItemStackLimit(itemstack10)) {
								k2 = slot7.getItemStackLimit(itemstack10);
							}

							if (itemstack10.stackSize >= k2) {
								slot7.putStack(itemstack10.splitStack(k2));
							}

							if (itemstack10.stackSize == 0) {
								inventoryplayer.setItemStack((ItemStack) null);
							}
						}
					} else if (slot7.canTakeStack(playerIn)) {
						if (itemstack10 == null) {
							int j2 = clickedButton == 0 ? itemstack9.stackSize : (itemstack9.stackSize + 1) / 2;
							ItemStack itemstack12 = slot7.decrStackSize(j2);
							inventoryplayer.setItemStack(itemstack12);
							if (itemstack9.stackSize == 0) {
								slot7.putStack((ItemStack) null);
							}

							slot7.onPickupFromSlot(playerIn, inventoryplayer.getItemStack());
						} else if (slot7.isItemValid(itemstack10)) {
							if (itemstack9.getItem() == itemstack10.getItem()
									&& itemstack9.getMetadata() == itemstack10.getMetadata()
									&& ItemStack.areItemStackTagsEqual(itemstack9, itemstack10)) {
								int i2 = clickedButton == 0 ? itemstack10.stackSize : 1;
								if (i2 > slot7.getItemStackLimit(itemstack10) - itemstack9.stackSize) {
									i2 = slot7.getItemStackLimit(itemstack10) - itemstack9.stackSize;
								}

								if (i2 > itemstack10.getMaxStackSize() - itemstack9.stackSize) {
									i2 = itemstack10.getMaxStackSize() - itemstack9.stackSize;
								}

								itemstack10.splitStack(i2);
								if (itemstack10.stackSize == 0) {
									inventoryplayer.setItemStack((ItemStack) null);
								}

								itemstack9.stackSize += i2;
							} else if (itemstack10.stackSize <= slot7.getItemStackLimit(itemstack10)) {
								slot7.putStack(itemstack10);
								inventoryplayer.setItemStack(itemstack9);
							}
						} else if (itemstack9.getItem() == itemstack10.getItem() && itemstack10.getMaxStackSize() > 1
								&& (!itemstack9.getHasSubtypes()
										|| itemstack9.getMetadata() == itemstack10.getMetadata())
								&& ItemStack.areItemStackTagsEqual(itemstack9, itemstack10)) {
							int l1 = itemstack9.stackSize;
							if (l1 > 0 && l1 + itemstack10.stackSize <= itemstack10.getMaxStackSize()) {
								itemstack10.stackSize += l1;
								itemstack9 = slot7.decrStackSize(l1);
								if (itemstack9.stackSize == 0) {
									slot7.putStack((ItemStack) null);
								}

								slot7.onPickupFromSlot(playerIn, inventoryplayer.getItemStack());
							}
						}
					}

					slot7.onSlotChanged();
				}
			}
		} else if (mode == 2 && clickedButton >= 0 && clickedButton < 9) {
			Slot slot5 = (Slot) this.inventorySlots.get(slotId);
			if (slot5.canTakeStack(playerIn)) {
				ItemStack itemstack7 = inventoryplayer.getStackInSlot(clickedButton);
				boolean flag = itemstack7 == null
						|| slot5.inventory == inventoryplayer && slot5.isItemValid(itemstack7);
				int k1 = -1;
				if (!flag) {
					k1 = inventoryplayer.getFirstEmptyStack();
					flag |= k1 > -1;
				}

				if (slot5.getHasStack() && flag) {
					ItemStack itemstack11 = slot5.getStack();
					inventoryplayer.setInventorySlotContents(clickedButton, itemstack11.copy());
					if ((slot5.inventory != inventoryplayer || !slot5.isItemValid(itemstack7)) && itemstack7 != null) {
						if (k1 > -1) {
							inventoryplayer.addItemStackToInventory(itemstack7);
							slot5.decrStackSize(itemstack11.stackSize);
							slot5.putStack((ItemStack) null);
							slot5.onPickupFromSlot(playerIn, itemstack11);
						}
					} else {
						slot5.decrStackSize(itemstack11.stackSize);
						slot5.putStack(itemstack7);
						slot5.onPickupFromSlot(playerIn, itemstack11);
					}
				} else if (!slot5.getHasStack() && itemstack7 != null && slot5.isItemValid(itemstack7)) {
					inventoryplayer.setInventorySlotContents(clickedButton, (ItemStack) null);
					slot5.putStack(itemstack7);
				}
			}
		} else if (mode == 3 && playerIn.capabilities.isCreativeMode && inventoryplayer.getItemStack() == null
				&& slotId >= 0) {
			Slot slot4 = (Slot) this.inventorySlots.get(slotId);
			if (slot4 != null && slot4.getHasStack()) {
				ItemStack itemstack6 = slot4.getStack().copy();
				itemstack6.stackSize = itemstack6.getMaxStackSize();
				inventoryplayer.setItemStack(itemstack6);
			}
		} else if (mode == 4 && inventoryplayer.getItemStack() == null && slotId >= 0) {
			Slot slot3 = (Slot) this.inventorySlots.get(slotId);
			if (slot3 != null && slot3.getHasStack() && slot3.canTakeStack(playerIn)) {
				ItemStack itemstack5 = slot3.decrStackSize(clickedButton == 0 ? 1 : slot3.getStack().stackSize);
				slot3.onPickupFromSlot(playerIn, itemstack5);
				playerIn.dropPlayerItemWithRandomChoice(itemstack5, true);
			}
		} else if (mode == 6 && slotId >= 0) {
			Slot slot2 = (Slot) this.inventorySlots.get(slotId);
			ItemStack itemstack4 = inventoryplayer.getItemStack();
			if (itemstack4 != null && (slot2 == null || !slot2.getHasStack() || !slot2.canTakeStack(playerIn))) {
				int i1 = clickedButton == 0 ? 0 : this.inventorySlots.size() - 1;
				int j1 = clickedButton == 0 ? 1 : -1;

				for (int l2 = 0; l2 < 2; ++l2) {
					for (int i3 = i1; i3 >= 0 && i3 < this.inventorySlots.size()
							&& itemstack4.stackSize < itemstack4.getMaxStackSize(); i3 += j1) {
						Slot slot8 = (Slot) this.inventorySlots.get(i3);
						if (slot8.getHasStack() && canAddItemToSlot(slot8, itemstack4, true)
								&& slot8.canTakeStack(playerIn) && this.canMergeSlot(itemstack4, slot8)
								&& (l2 != 0 || slot8.getStack().stackSize != slot8.getStack().getMaxStackSize())) {
							int l = Math.min(itemstack4.getMaxStackSize() - itemstack4.stackSize,
									slot8.getStack().stackSize);
							ItemStack itemstack2 = slot8.decrStackSize(l);
							itemstack4.stackSize += l;
							if (itemstack2.stackSize <= 0) {
								slot8.putStack((ItemStack) null);
							}

							slot8.onPickupFromSlot(playerIn, itemstack2);
						}
					}
				}
			}

			this.detectAndSendChanges();
		}

		return itemstack;
	}

	/**+
	 * Called to determine if the current slot is valid for the
	 * stack merging (double-click) code. The stack passed in is
	 * null for the initial slot that was double-clicked.
	 */
	public boolean canMergeSlot(ItemStack var1, Slot var2) {
		return true;
	}

	/**+
	 * Retries slotClick() in case of failure
	 */
	protected void retrySlotClick(int i, int j, boolean var3, EntityPlayer entityplayer) {
		this.slotClick(i, j, 1, entityplayer);
	}

	/**+
	 * Called when the container is closed.
	 */
	public void onContainerClosed(EntityPlayer playerIn) {
		InventoryPlayer inventoryplayer = playerIn.inventory;
		if (inventoryplayer.getItemStack() != null) {
			playerIn.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack(), false);
			inventoryplayer.setItemStack((ItemStack) null);
		}

	}

	/**+
	 * Callback for when the crafting matrix is changed.
	 */
	public void onCraftMatrixChanged(IInventory inventoryIn) {
		this.detectAndSendChanges();
	}

	/**+
	 * args: slotID, itemStack to put in slot
	 */
	public void putStackInSlot(int slotID, ItemStack stack) {
		this.getSlot(slotID).putStack(stack);
	}

	/**+
	 * places itemstacks in first x slots, x being aitemstack.lenght
	 */
	public void putStacksInSlots(ItemStack[] parArrayOfItemStack) {
		for (int i = 0; i < parArrayOfItemStack.length; ++i) {
			this.getSlot(i).putStack(parArrayOfItemStack[i]);
		}

	}

	public void updateProgressBar(int id, int data) {
	}

	/**+
	 * Gets a unique transaction ID. Parameter is unused.
	 */
	public short getNextTransactionID(InventoryPlayer parInventoryPlayer) {
		++this.transactionID;
		return this.transactionID;
	}

	/**+
	 * gets whether or not the player can craft in this inventory or
	 * not
	 */
	public boolean getCanCraft(EntityPlayer parEntityPlayer) {
		return !this.playerList.contains(parEntityPlayer);
	}

	/**+
	 * sets whether the player can craft in this inventory or not
	 */
	public void setCanCraft(EntityPlayer parEntityPlayer, boolean parFlag) {
		if (parFlag) {
			this.playerList.remove(parEntityPlayer);
		} else {
			this.playerList.add(parEntityPlayer);
		}

	}

	public abstract boolean canInteractWith(EntityPlayer var1);

	/**+
	 * Merges provided ItemStack with the first avaliable one in the
	 * container/player inventor between minIndex (included) and
	 * maxIndex (excluded). Args : stack, minIndex, maxIndex,
	 * negativDirection. /!\ the Container implementation do not
	 * check if the item is valid for the slot
	 */
	protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
		boolean flag = false;
		int i = startIndex;
		if (reverseDirection) {
			i = endIndex - 1;
		}

		if (stack.isStackable()) {
			while (stack.stackSize > 0 && (!reverseDirection && i < endIndex || reverseDirection && i >= startIndex)) {
				Slot slot = (Slot) this.inventorySlots.get(i);
				ItemStack itemstack = slot.getStack();
				if (itemstack != null && itemstack.getItem() == stack.getItem()
						&& (!stack.getHasSubtypes() || stack.getMetadata() == itemstack.getMetadata())
						&& ItemStack.areItemStackTagsEqual(stack, itemstack)) {
					int j = itemstack.stackSize + stack.stackSize;
					if (j <= stack.getMaxStackSize()) {
						stack.stackSize = 0;
						itemstack.stackSize = j;
						slot.onSlotChanged();
						flag = true;
					} else if (itemstack.stackSize < stack.getMaxStackSize()) {
						stack.stackSize -= stack.getMaxStackSize() - itemstack.stackSize;
						itemstack.stackSize = stack.getMaxStackSize();
						slot.onSlotChanged();
						flag = true;
					}
				}

				if (reverseDirection) {
					--i;
				} else {
					++i;
				}
			}
		}

		if (stack.stackSize > 0) {
			if (reverseDirection) {
				i = endIndex - 1;
			} else {
				i = startIndex;
			}

			while (!reverseDirection && i < endIndex || reverseDirection && i >= startIndex) {
				Slot slot1 = (Slot) this.inventorySlots.get(i);
				ItemStack itemstack1 = slot1.getStack();
				if (itemstack1 == null) {
					slot1.putStack(stack.copy());
					slot1.onSlotChanged();
					stack.stackSize = 0;
					flag = true;
					break;
				}

				if (reverseDirection) {
					--i;
				} else {
					++i;
				}
			}
		}

		return flag;
	}

	/**+
	 * Extracts the drag mode. Args : eventButton. Return (0 :
	 * evenly split, 1 : one item by slot, 2 : not used ?)
	 */
	public static int extractDragMode(int parInt1) {
		return parInt1 >> 2 & 3;
	}

	/**+
	 * Args : clickedButton, Returns (0 : start drag, 1 : add slot,
	 * 2 : end drag)
	 */
	public static int getDragEvent(int parInt1) {
		return parInt1 & 3;
	}

	public static int func_94534_d(int parInt1, int parInt2) {
		return parInt1 & 3 | (parInt2 & 3) << 2;
	}

	public static boolean isValidDragMode(int dragModeIn, EntityPlayer player) {
		return dragModeIn == 0 ? true
				: (dragModeIn == 1 ? true : dragModeIn == 2 && player.capabilities.isCreativeMode);
	}

	/**+
	 * Reset the drag fields
	 */
	protected void resetDrag() {
		this.dragEvent = 0;
		this.dragSlots.clear();
	}

	/**+
	 * Checks if it's possible to add the given itemstack to the
	 * given slot.
	 */
	public static boolean canAddItemToSlot(Slot slotIn, ItemStack stack, boolean stackSizeMatters) {
		boolean flag = slotIn == null || !slotIn.getHasStack();
		if (slotIn != null && slotIn.getHasStack() && stack != null && stack.isItemEqual(slotIn.getStack())
				&& ItemStack.areItemStackTagsEqual(slotIn.getStack(), stack)) {
			flag |= slotIn.getStack().stackSize + (stackSizeMatters ? 0 : stack.stackSize) <= stack.getMaxStackSize();
		}

		return flag;
	}

	/**+
	 * Compute the new stack size, Returns the stack with the new
	 * size. Args : dragSlots, dragMode, dragStack, slotStackSize
	 */
	public static void computeStackSize(Set<Slot> parSet, int parInt1, ItemStack parItemStack, int parInt2) {
		switch (parInt1) {
		case 0:
			parItemStack.stackSize = MathHelper.floor_float((float) parItemStack.stackSize / (float) parSet.size());
			break;
		case 1:
			parItemStack.stackSize = 1;
			break;
		case 2:
			parItemStack.stackSize = parItemStack.getItem().getItemStackLimit();
		}

		parItemStack.stackSize += parInt2;
	}

	/**+
	 * Returns true if the player can "drag-spilt" items into this
	 * slot,. returns true by default. Called to check if the slot
	 * can be added to a list of Slots to split the held ItemStack
	 * across.
	 */
	public boolean canDragIntoSlot(Slot var1) {
		return true;
	}

	/**+
	 * Like the version that takes an inventory. If the given
	 * TileEntity is not an Inventory, 0 is returned instead.
	 */
	public static int calcRedstone(TileEntity te) {
		return te instanceof IInventory ? calcRedstoneFromInventory((IInventory) te) : 0;
	}

	public static int calcRedstoneFromInventory(IInventory inv) {
		if (inv == null) {
			return 0;
		} else {
			int i = 0;
			float f = 0.0F;

			for (int j = 0; j < inv.getSizeInventory(); ++j) {
				ItemStack itemstack = inv.getStackInSlot(j);
				if (itemstack != null) {
					f += (float) itemstack.stackSize
							/ (float) Math.min(inv.getInventoryStackLimit(), itemstack.getMaxStackSize());
					++i;
				}
			}

			f = f / (float) inv.getSizeInventory();
			return MathHelper.floor_float(f * 14.0F) + (i > 0 ? 1 : 0);
		}
	}
}
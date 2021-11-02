/*
 * file: Block.java
 * author: N. Baron
 * class: CS 4450 - Computer Graphics
 *
 * assignment: Project
 * date last modified: 10/2/2021
 *
 * purpose: This class represents 1 block in a chunk.
 */
package cs4450_project;

public class Block {

    private boolean isActive;
    private final BlockType type;
    private float x, y, z;

    // method: constructor
    // purpose: initialize the type of the block
    public Block(BlockType type) {
        this.type = type;
    }

    // method: getID
    // purpose: return the ID of the block
    public int getID() {
        return type.getID();
    }

    // method: getType
    // purpose: return the type of the block
    public BlockType getType(){
        return this.type;
    }

    // method: isActive
    // purpose: return whether the block is active
    public boolean isActive() {
        return isActive;
    }

    // method: setActive
    // purpose: set whether the block is active
    public void setActive(boolean active) {
        isActive = active;
    }

    // method: setCoords
    // purpose: set the coordinates of the block
    public void setCoords(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}

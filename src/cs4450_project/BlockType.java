/*
 * file: BlockType.java
 * author: N. Baron, D. Faizi
 * class: CS 4450 - Computer Graphics
 *
 * assignment: Project
 * date last modified: 11/26/2021
 *
 * purpose: This class stores data for a single type of block and plant.
 */
package cs4450_project;

public enum BlockType {

    Grass(0), Sand(1), Water(2), Dirt(3), Stone(4), Bedrock(5), YellowFlower(6), TallGrass(7), TreeSapling(8), RedFlower(9);

    private int blockID;

    // method: constructor
    // purpose: initialize the ID of the blocktype
    private BlockType(int id) {
        blockID = id;
    }

    // method: getID
    // purpose: return the ID of the blocktype
    public int getID() {
        return blockID;
    }

    // method: setID
    // purpose: set the ID of the blocktype
    public void setID(int id) {
        blockID = id;
    }

}

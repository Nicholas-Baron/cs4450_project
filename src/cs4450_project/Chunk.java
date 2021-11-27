/*
 * file: Chunks.java
 * author: D. Faizi, N. Baron
 * class: CS 4450 - Computer Graphics
 *
 * assignment: Project
 * date last modified: 11/26/2021
 *
 * purpose: This class represents a chunk in 3D space. Each block, within
 * the chunk, is given a texture from the terrian.png file. Plant block types
 * follow the same process, but done separately. Adding lakes/rivers are also 
 * implemented in this class.
 */
package cs4450_project;

import java.nio.FloatBuffer;
import java.util.Random;
import org.lwjgl.BufferUtils;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public final class Chunk 
{

    public static final int CHUNK_SIZE = 30;
    private static final int CUBES_PER_BLOCK = CHUNK_SIZE * CHUNK_SIZE
        * CHUNK_SIZE;
    public static final int CUBE_LENGTH = 2;
    public static final int CHUNK_LENGTH = CHUNK_SIZE * CUBE_LENGTH;

    private static SimplexNoise simplexNoise;

    public final Block[][][] Blocks;
    private int VBOColorHandle;
    private int VBOTextureHandle;
    private int VBOVertexHandle;
    private static Texture texture;
    private final int posX,posZ;

    // method: Chunk
    // purpose: This method is the constructor for our chunk class.
    public Chunk(int startX, int startZ) 
    {
        if (texture == null) try {
            texture = TextureLoader.getTexture("PNG",
                ResourceLoader.getResourceAsStream("res/terrain.png"));
        } catch (Exception e) {
            System.out.print("ER-ROAR!");
        }
        Random r = new Random();

        if (simplexNoise == null)
            simplexNoise = new SimplexNoise(CHUNK_SIZE * 2, 0.25, r.nextInt());
        
        Blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
        for (int x = 0; x < CHUNK_SIZE; x++) 
        {
            for (int z = 0; z < CHUNK_SIZE; z++) 
            {
                double height = simplexNoise.getNoise(startX + x, startZ + z)
                    * CHUNK_SIZE
                    + CHUNK_SIZE/2;
                for (int y = 0; y < CHUNK_SIZE; y++) 
                {
                    //base of terrain consist of bedrock blocks
                    if (y == 0) 
                    {
                        Blocks[x][y][z] = new Block(BlockType.Bedrock);
                    }
                    //top of terrain (above y = 16 ) consist of grass blocks
                    else if (y == Math.floor(height)  && y >= 16) 
                    {
                        Blocks[x][y][z] = new Block(BlockType.Grass);
                    }
                    // Between the y values 14 - 16, consist of sand blocks 
                    else if (y == Math.floor(height) && y < 16 && y >= 14) 
                    {
                        Blocks[x][y][z] = new Block(BlockType.Sand);
                    }
                    // Water blocks on the outer edges (below y = 14) of the terrain surface
                    else if (y == Math.floor(height) && y < 14 && y > 0) 
                    {
                        Blocks[x][y][z] = new Block(BlockType.Water);
                    }
                    // Adds water blocks to the open valleys of the terrain (below y = 14), simulates a lake/river
                    else if (y > Math.floor(height) && Math.floor(height) < 14 && y < 14) 
                    {
                        Blocks[x][y][z] = new Block(BlockType.Water);
                    }
                    // All Plants on surface of terrain
                    else if ( y == Math.floor(height)+1 && Blocks[x][y-1][z].getType() == BlockType.Grass)
                    {
                        float blockType = r.nextFloat();
                        if (blockType <= 0.20) {
                            Blocks[x][y][z] = new Block(BlockType.YellowFlower);
                        } else if (blockType <= 0.44) {
                            Blocks[x][y][z] = new Block(BlockType.TreeSapling);
                        } 
                        else if (blockType <= 0.66) {
                            Blocks[x][y][z] = new Block(BlockType.RedFlower);
                        }else {
                            Blocks[x][y][z] = new Block(BlockType.TallGrass);
                        }
                    }
                    // In the middle of terrain (below the surface) consist of Stone and Dirt Blocks 
                    else {
                        // somewhere in the middle.
                        // dirt or stone
                        Blocks[x][y][z] = new Block(
                            r.nextBoolean() ? BlockType.Dirt : BlockType.Stone
                        );
                    }
                    // Set the lake blocks to Active (able to add texture)
                    Block b = Blocks[x][y][z];
                    b.setActive(y <= height);
                    if (y > Math.floor(height) && Math.floor(height) < 14 && y < 14) 
                    {
                        b.setActive(true);
                    }
                    // Set the plant blocks to Active (able to add texture)
                    else if (b.getType() == BlockType.TreeSapling || b.getType() == 
                                BlockType.YellowFlower || b.getType() == 
                                BlockType.RedFlower || b.getType() == 
                                BlockType.TallGrass)
                    {
                        b.setActive(true);
                    }
                }
            }
        }
        posX = startX;
        posZ = startZ;
        rebuildMesh(startX, startZ);
    }

    // purpose: containsBlock
    // method: returns whether the x,z coordinate refers to
    // some block inside this chunk
    public boolean containsBlock(int x, int z){
        return posX <= x && x < posX + CHUNK_SIZE
            && posZ <= z && z < posZ + CHUNK_SIZE;
    }

    // purpose: getBlockDistance
    // method: returns the 2D distance
    // on the xz plane from the center of the chunk
    public double getBlockDistance(int x, int z){
        int xDist = (posX + CHUNK_SIZE / 2) + x;
        int zDist = (posZ + CHUNK_SIZE / 2) + z;
        return Math.sqrt(xDist * xDist + zDist * zDist);
    }

    // method: createCubeVertexCol
    // purpose: This method specifies what color the individual
    // cube should be.
    private float[] createCubeVertexCol(float[] CubeColorArray) {
        float[] cubeColors = new float[CubeColorArray.length * 4 * 6];
        for (int i = 0; i < cubeColors.length; i++) {
            cubeColors[i] = CubeColorArray[i % CubeColorArray.length];
        }
        return cubeColors;
    }

    // method: getCubeColor
    // purpose: This method returns a color for the cube.
    private float[] getCubeColor(Block block) {
        return new float[]{1, 1, 1};
    }

    // method: rebuildMesh
    // purpose: This method modifies our chunk after it has
    // been initially created.
    public void rebuildMesh(float startX, float startZ) 
    {
        VBOColorHandle = glGenBuffers();
        VBOVertexHandle = glGenBuffers();
        VBOTextureHandle = glGenBuffers();
        int bufferSize = CUBES_PER_BLOCK * 6 * 12;
        FloatBuffer VertexPositionData = BufferUtils.createFloatBuffer(
            bufferSize
        );
        FloatBuffer VertexColorData = BufferUtils.createFloatBuffer(bufferSize);
        FloatBuffer VertexTextureData = BufferUtils.createFloatBuffer(
            bufferSize
        );
        // Block types: grass, dirt, sand, water, bedrock, stone are processed here
        for (float x = 0; x < CHUNK_SIZE; ++x) {
            
   
               for (float z = 0; z < CHUNK_SIZE; ++z) {
                   for (float y = 0; y < CHUNK_SIZE; ++y) {
                       Block b = Blocks[(int) x][(int) y][(int) z];
                       if (!b.isActive())
                       {
                           continue;
                       }
                       // If the block type is a plant, then continue.
                       if (b.getType() == BlockType.TreeSapling || b.getType() == 
                                BlockType.YellowFlower || b.getType() == 
                                BlockType.RedFlower || b.getType() == 
                                BlockType.TallGrass)
                        {
                        continue;
                        }
                       
                        VertexPositionData.put(createCube(
                            (startX + x) * CUBE_LENGTH,
                            y * CUBE_LENGTH + CHUNK_SIZE * .8f,
                            (startZ + z) * CUBE_LENGTH));
                        VertexColorData.put(createCubeVertexCol(getCubeColor(b)));
                        VertexTextureData.put(createTexCube(0, 0, b));  
                   }
               }
           }
            // Adds transparency to Plant textures (alpha set to 0)
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            // Plant block types: redflower, yellowflower, tallGrass, treesapling
            for (float x = 0; x < CHUNK_SIZE; ++x)
            {
                for (float z = 0; z < CHUNK_SIZE; ++z) 
                {
                    for (float y = 0; y < CHUNK_SIZE; ++y) 
                    {
                        Block b = Blocks[(int) x][(int) y][(int) z];
                        // if block types is not a plant, then continue
                        if (b.getType() != BlockType.TreeSapling && b.getType() != 
                                BlockType.YellowFlower&& b.getType() != 
                                BlockType.RedFlower&& b.getType() != 
                                BlockType.TallGrass)
                        {
                            continue;
                        }

                        if (!b.isActive()) {
                            continue;
                        }
                            VertexPositionData.put(createPlant(
                            (startX + x) * CUBE_LENGTH,
                            y * CUBE_LENGTH + CHUNK_SIZE * .8f,
                            (startZ + z) * CUBE_LENGTH
                            ));
                            VertexTextureData.put(createTexPlant(0, 0, b));
                    }
                }
            }
        VertexColorData.flip();
        VertexPositionData.flip();
        VertexTextureData.flip();
        glBindBuffer(GL_ARRAY_BUFFER, VBOVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexPositionData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, VBOColorHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexColorData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexTextureData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    // method: render
    // purpose: This method draws the chunks (blocks and plants with a texture).
    public void render() {
        glPushMatrix();
        glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
        glBindTexture(GL_TEXTURE_2D, 1);
        glTexCoordPointer(2, GL_FLOAT, 0, 0L);
        glBindBuffer(GL_ARRAY_BUFFER, VBOVertexHandle);
        glVertexPointer(3, GL_FLOAT, 0, 0L);
        glBindBuffer(GL_ARRAY_BUFFER, VBOColorHandle);
        glColorPointer(3, GL_FLOAT, 0, 0L);
        glDrawArrays(GL_QUADS, 0, CUBES_PER_BLOCK * 24);
        glPopMatrix();
    }

    // method: createCube
    // purpose: This method creates a cube.
    public static float[] createCube(float x, float y, float z) {
        int offset = CUBE_LENGTH / 2;
        return new float[]{
            // TOP QUAD
            x + offset, y + offset, z,
            x - offset, y + offset, z,
            x - offset, y + offset, z - CUBE_LENGTH,
            x + offset, y + offset, z - CUBE_LENGTH,
            // BOTTOM QUAD
            x + offset, y - offset, z - CUBE_LENGTH,
            x - offset, y - offset, z - CUBE_LENGTH,
            x - offset, y - offset, z,
            x + offset, y - offset, z,
            // FRONT QUAD
            x + offset, y + offset, z - CUBE_LENGTH,
            x - offset, y + offset, z - CUBE_LENGTH,
            x - offset, y - offset, z - CUBE_LENGTH,
            x + offset, y - offset, z - CUBE_LENGTH,
            // BACK QUAD
            x + offset, y - offset, z,
            x - offset, y - offset, z,
            x - offset, y + offset, z,
            x + offset, y + offset, z,
            // LEFT QUAD
            x - offset, y + offset, z - CUBE_LENGTH,
            x - offset, y + offset, z,
            x - offset, y - offset, z,
            x - offset, y - offset, z - CUBE_LENGTH,
            // RIGHT QUAD
            x + offset, y + offset, z,
            x + offset, y + offset, z - CUBE_LENGTH,
            x + offset, y - offset, z - CUBE_LENGTH,
            x + offset, y - offset, z};
    }

    // method: createTexCube
    // purpose: This method uses the textures
    // (from the  terrian.png file) and adds the specified texture
    //(depending on the case) onto all 6 sides of the block.
    public static float[] createTexCube(float x, float y, Block block) {
        float offset = (1024f / 16) / 1024f;
        switch (block.getType()) {
            case Grass: // 0 in the original code
                return new float[]{
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset * 3, y + offset * 10,
                    x + offset * 2, y + offset * 10,
                    x + offset * 2, y + offset * 9,
                    x + offset * 3, y + offset * 9,
                    // TOP!
                    x + offset * 3, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    x + offset * 2, y + offset * 0,
                    x + offset * 3, y + offset * 0,
                    // FRONT QUAD
                    x + offset * 3, y + offset * 0,
                    x + offset * 4, y + offset * 0,
                    x + offset * 4, y + offset * 1,
                    x + offset * 3, y + offset * 1,
                    // BACK QUAD
                    x + offset * 4, y + offset * 1,
                    x + offset * 3, y + offset * 1,
                    x + offset * 3, y + offset * 0,
                    x + offset * 4, y + offset * 0,
                    // LEFT QUAD
                    x + offset * 3, y + offset * 0,
                    x + offset * 4, y + offset * 0,
                    x + offset * 4, y + offset * 1,
                    x + offset * 3, y + offset * 1,
                    // RIGHT QUAD
                    x + offset * 3, y + offset * 0,
                    x + offset * 4, y + offset * 0,
                    x + offset * 4, y + offset * 1,
                    x + offset * 3, y + offset * 1};
            case Sand: // 1
                return new float[]{
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset * 3, y + offset * 2,
                    x + offset * 2, y + offset * 2,
                    x + offset * 2, y + offset * 1,
                    x + offset * 3, y + offset * 1,
                    // TOP!
                    x + offset * 3, y + offset * 2,
                    x + offset * 2, y + offset * 2,
                    x + offset * 2, y + offset * 1,
                    x + offset * 3, y + offset * 1,
                    // FRONT QUAD
                    x + offset * 3, y + offset * 2,
                    x + offset * 2, y + offset * 2,
                    x + offset * 2, y + offset * 1,
                    x + offset * 3, y + offset * 1,
                    // BACK QUAD
                    x + offset * 3, y + offset * 2,
                    x + offset * 2, y + offset * 2,
                    x + offset * 2, y + offset * 1,
                    x + offset * 3, y + offset * 1,
                    // LEFT QUAD
                    x + offset * 3, y + offset * 2,
                    x + offset * 2, y + offset * 2,
                    x + offset * 2, y + offset * 1,
                    x + offset * 3, y + offset * 1,
                    // RIGHT QUAD
                    x + offset * 3, y + offset * 2,
                    x + offset * 2, y + offset * 2,
                    x + offset * 2, y + offset * 1,
                    x + offset * 3, y + offset * 1};
            case Dirt:
                return new float[]{
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset * 3, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    x + offset * 2, y + offset * 0,
                    x + offset * 3, y + offset * 0,
                    // TOP!
                    x + offset * 3, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    x + offset * 2, y + offset * 0,
                    x + offset * 3, y + offset * 0,
                    // FRONT QUAD
                    x + offset * 3, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    x + offset * 2, y + offset * 0,
                    x + offset * 3, y + offset * 0,
                    // BACK QUAD
                    x + offset * 3, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    x + offset * 2, y + offset * 0,
                    x + offset * 3, y + offset * 0,
                    // LEFT QUAD
                    x + offset * 3, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    x + offset * 2, y + offset * 0,
                    x + offset * 3, y + offset * 0,
                    // RIGHT QUAD
                    x + offset * 3, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    x + offset * 2, y + offset * 0,
                    x + offset * 3, y + offset * 0,};
            case Water: // 3
                return new float[]{
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset * 15, y + offset * 1,
                    x + offset * 14, y + offset * 1,
                    x + offset * 14, y + offset * 0,
                    x + offset * 15, y + offset * 0,
                    // TOP!
                    x + offset * 15, y + offset * 1,
                    x + offset * 14, y + offset * 1,
                    x + offset * 14, y + offset * 0,
                    x + offset * 15, y + offset * 0,
                    // FRONT QUAD
                    x + offset * 15, y + offset * 1,
                    x + offset * 14, y + offset * 1,
                    x + offset * 14, y + offset * 0,
                    x + offset * 15, y + offset * 0,
                    // BACK QUAD
                    x + offset * 15, y + offset * 1,
                    x + offset * 14, y + offset * 1,
                    x + offset * 14, y + offset * 0,
                    x + offset * 15, y + offset * 0,
                    // LEFT QUAD
                    x + offset * 15, y + offset * 1,
                    x + offset * 14, y + offset * 1,
                    x + offset * 14, y + offset * 0,
                    x + offset * 15, y + offset * 0,
                    // RIGHT QUAD
                    x + offset * 15, y + offset * 1,
                    x + offset * 14, y + offset * 1,
                    x + offset * 14, y + offset * 0,
                    x + offset * 15, y + offset * 0};
            case Stone: // 4
                return new float[]{
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset * 2, y + offset * 1,
                    x + offset * 1, y + offset * 1,
                    x + offset * 1, y + offset * 0,
                    x + offset * 2, y + offset * 0,
                    // TOP!
                    x + offset * 2, y + offset * 1,
                    x + offset * 1, y + offset * 1,
                    x + offset * 1, y + offset * 0,
                    x + offset * 2, y + offset * 0,
                    // FRONT QUAD
                    x + offset * 2, y + offset * 1,
                    x + offset * 1, y + offset * 1,
                    x + offset * 1, y + offset * 0,
                    x + offset * 2, y + offset * 0,
                    // BACK QUAD
                    x + offset * 2, y + offset * 1,
                    x + offset * 1, y + offset * 1,
                    x + offset * 1, y + offset * 0,
                    x + offset * 2, y + offset * 0,
                    // LEFT QUAD
                    x + offset * 2, y + offset * 1,
                    x + offset * 1, y + offset * 1,
                    x + offset * 1, y + offset * 0,
                    x + offset * 2, y + offset * 0,
                    // RIGHT QUAD
                    x + offset * 2, y + offset * 1,
                    x + offset * 1, y + offset * 1,
                    x + offset * 1, y + offset * 0,
                    x + offset * 2, y + offset * 0};
            case Bedrock: // 5
                return new float[]{
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset * 2, y + offset * 2,
                    x + offset * 1, y + offset * 2,
                    x + offset * 1, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    // TOP!
                    x + offset * 2, y + offset * 2,
                    x + offset * 1, y + offset * 2,
                    x + offset * 1, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    // FRONT QUAD
                    x + offset * 2, y + offset * 2,
                    x + offset * 1, y + offset * 2,
                    x + offset * 1, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    // BACK QUAD
                    x + offset * 2, y + offset * 2,
                    x + offset * 1, y + offset * 2,
                    x + offset * 1, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    // LEFT QUAD
                    x + offset * 2, y + offset * 2,
                    x + offset * 1, y + offset * 2,
                    x + offset * 1, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    // RIGHT QUAD
                    x + offset * 2, y + offset * 2,
                    x + offset * 1, y + offset * 2,
                    x + offset * 1, y + offset * 1,
                    x + offset * 2, y + offset * 1};
        }
        return new float[]{
            // BOTTOM QUAD(DOWN=+Y)
            x + offset * 3, y + offset * 10,
            x + offset * 2, y + offset * 10,
            x + offset * 2, y + offset * 9,
            x + offset * 3, y + offset * 9,
            // TOP!
            x + offset * 3, y + offset * 1,
            x + offset * 2, y + offset * 1,
            x + offset * 2, y + offset * 0,
            x + offset * 3, y + offset * 0,
            // FRONT QUAD
            x + offset * 3, y + offset * 0,
            x + offset * 4, y + offset * 0,
            x + offset * 4, y + offset * 1,
            x + offset * 3, y + offset * 1,
            // BACK QUAD
            x + offset * 4, y + offset * 1,
            x + offset * 3, y + offset * 1,
            x + offset * 3, y + offset * 0,
            x + offset * 4, y + offset * 0,
            // LEFT QUAD
            x + offset * 3, y + offset * 0,
            x + offset * 4, y + offset * 0,
            x + offset * 4, y + offset * 1,
            x + offset * 3, y + offset * 1,
            // RIGHT QUAD
            x + offset * 3, y + offset * 0,
            x + offset * 4, y + offset * 0,
            x + offset * 4, y + offset * 1,
            x + offset * 3, y + offset * 1};
    }
    
    // method: createPlant
    // purpose: This method creates a 3D plus shaped object.
    public static float[] createPlant(float x, float y, float z) {
        int offset = CUBE_LENGTH / 2;
        return new float[]{
            // FRONT CENTER QUAD
            (x + offset), y + offset, z - (CUBE_LENGTH/2),
            (x - offset), y + offset, z - (CUBE_LENGTH/2),
            (x - offset), y - offset, z - (CUBE_LENGTH/2),
            (x + offset), y - offset, z - (CUBE_LENGTH/2),
            // LEFT CENTER QUAD
            x - (offset/2), (y + offset), z - (CUBE_LENGTH),
            x - (offset/2), (y + offset), z,
            x - (offset/2), (y - offset), z,
            x - (offset/2), (y - offset), z - (CUBE_LENGTH),
        };
    }
    // method: createTexPlant
    // purpose: This method uses the textures
    // (from the  terrian.png file) and adds the specified texture
    //(depending on the case) onto the two square surfaces of the plant.
    public static float[] createTexPlant(float x, float y, Block block) {
        float offset = (1024f / 16) / 1024f;
        switch (block.getType()) {
            case YellowFlower: // 6
                return new float[]{
                    // FRONT CENTER QUAD
                    x + offset * 14, y + offset * 0,
                    x + offset * 13, y + offset * 0,
                    x + offset * 13, y + offset * 1,
                    x + offset * 14, y + offset * 1,
                    // LEFT CENTER QUAD
                    x + offset * 14, y + offset * 0,
                    x + offset * 13, y + offset * 0,
                    x + offset * 13, y + offset * 1,
                    x + offset * 14, y + offset * 1,
                };
                case TallGrass: // 7
                return new float[]{
                    // FRONT CENTER QUAD
                    x + offset * 16, y + offset * 5,
                    x + offset * 15, y + offset * 5,
                    x + offset * 15, y + offset * 6,
                    x + offset * 16, y + offset * 6,
                    // LEFT CENTER QUAD
                    x + offset * 16, y + offset * 5,
                    x + offset * 15, y + offset * 5,
                    x + offset * 15, y + offset * 6,
                    x + offset * 16, y + offset * 6,
                };
                case TreeSapling: // 8
                return new float[]{
                    // FRONT CENTER QUAD
                    x + offset * 16, y + offset * 0,
                    x + offset * 15, y + offset * 0,
                    x + offset * 15, y + offset * 1,
                    x + offset * 16, y + offset * 1,
                    // LEFT CENTER QUAD
                    x + offset * 16, y + offset * 0,
                    x + offset * 15, y + offset * 0,
                    x + offset * 15, y + offset * 1,
                    x + offset * 16, y + offset * 1,
                };
                case RedFlower: // 8
                return new float[]{
                    // FRONT CENTER QUAD
                    x + offset * 13, y + offset * 0,
                    x + offset * 12, y + offset * 0,
                    x + offset * 12, y + offset * 1,
                    x + offset * 13, y + offset * 1,
                    // LEFT CENTER QUAD
                    x + offset * 13, y + offset * 0,
                    x + offset * 12, y + offset * 0,
                    x + offset * 12, y + offset * 1,
                    x + offset * 13, y + offset * 1,
                };
        }
        return new float[]{
                    // FRONT CENTERQUAD
                    x + offset * 16, y + offset * 0,
                    x + offset * 15, y + offset * 0,
                    x + offset * 15, y + offset * 1,
                    x + offset * 16, y + offset * 1,
                    // LEFT CENTER QUAD
                    x + offset * 16, y + offset * 0,
                    x + offset * 15, y + offset * 0,
                    x + offset * 15, y + offset * 1,
                    x + offset * 16, y + offset * 1,
        };
    }
}

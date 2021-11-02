/*
 * file: Chunks.java
 * author: D. Faizi
 * class: CS 4450 - Computer Graphics
 *
 * assignment: Project
 * date last modified: 11/1/2021
 *
 * purpose: This class represents a chunk in 3D space. Each block, within 
 * the chunk, is given a texture from the terrian.png file.
 */
package cs4450_project;

import java.nio.FloatBuffer;
import java.util.Random;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

public final class Chunk
{
    public static final int CHUNK_SIZE = 30;
    public static final int CUBE_LENGTH = 2;
    private static final int CUBES_PER_BLOCK = CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE;

    private final Block[][][] Blocks;
    private int VBOVertexHandle;
    private int VBOColorHandle;
    private int StartX, StartY, StartZ;
    private Random r;
    private SimplexNoise simplexNoise;
    private int VBOTextureHandle;
    private Texture texture;

    // method: render
    // purpose: This method draws the chunks (blocks with a texture). 
    public void render()
    {
        glPushMatrix();
        glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
        glBindTexture(GL_TEXTURE_2D, 1);
        glTexCoordPointer(2,GL_FLOAT,0,0L);
        glBindBuffer(GL_ARRAY_BUFFER, VBOVertexHandle);
        glVertexPointer(3, GL_FLOAT, 0, 0L);
        glBindBuffer(GL_ARRAY_BUFFER, VBOColorHandle);
        glColorPointer(3, GL_FLOAT, 0, 0L);
        glDrawArrays(GL_QUADS, 0, CUBES_PER_BLOCK * 24);
        glPopMatrix();
    }
    
    // method: rebuildMesh
    // purpose: This method modifies our chunk after it has
    // been initially created.
    public void rebuildMesh(float startX, float startY, float startZ)
    {
        VBOColorHandle = glGenBuffers();
        VBOVertexHandle = glGenBuffers();

        int bufferSize = CUBES_PER_BLOCK * 6 * 12;
        VBOTextureHandle = glGenBuffers();
        FloatBuffer VertexPositionData = 
                BufferUtils.createFloatBuffer(bufferSize);
        FloatBuffer VertexColorData =
                BufferUtils.createFloatBuffer(bufferSize);
        
        //the following among our other Float Buffers before our for loops
        FloatBuffer VertexTextureData =
        BufferUtils.createFloatBuffer(bufferSize);
        for (float x = 0; x < CHUNK_SIZE; ++x) {
            for (float z = 0; z < CHUNK_SIZE; ++z) {
                for (float y = 0; y < CHUNK_SIZE; y++) {
                    Block b = Blocks[(int) x][(int) y][(int) z];

                    if (!b.isActive()) {
                        continue;
                    }

                    VertexPositionData.put(createCube((float) 
                            (startX+ x * CUBE_LENGTH),
                            (float)(y*CUBE_LENGTH+(int)(CHUNK_SIZE*.8)),
                            (float) (startZ+ z * CUBE_LENGTH)));
                            
                            VertexColorData.put(
                                    createCubeVertexCol(
                                            getCubeColor(
                                                    b)));
                            VertexTextureData.put(
                                    createTexCube(
                                            (float) 0,
                                            (float)0,
                                            b));

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
        glBufferData(GL_ARRAY_BUFFER, VertexTextureData,GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
    
    // method: createCubeVertexCol
    // purpose: This method specifies what color the individual 
    // cube should be.
    private float[] createCubeVertexCol(float[] CubeColorArray)
    {
        float[] cubeColors = new float[CubeColorArray.length * 4 * 6];
        for (int i = 0; i < cubeColors.length; i++) {
            cubeColors[i] = CubeColorArray[i % CubeColorArray.length];
        }
        return cubeColors;
    }
    
    // method: createCube
    // purpose: This method creates a cube at the specified location.
    public static float[] createCube(float x, float y, float z)
    {
        int offset = CUBE_LENGTH / 2;
            return new float[] {
            // TOP QUAD
            x + offset, y + offset,z,
            x -offset, y + offset, z,
            x -offset, y + offset, z -CUBE_LENGTH,
            x + offset, y + offset, z -CUBE_LENGTH,
            // BOTTOM QUAD
            x + offset, y -offset, z -CUBE_LENGTH,
            x -offset, y -offset, z -CUBE_LENGTH,
            x -offset, y -offset, z,
            x + offset, y -offset, z,
            // FRONT QUAD
            x + offset, y + offset, z -CUBE_LENGTH,
            x -offset, y + offset, z -CUBE_LENGTH,
            x -offset, y -offset, z -CUBE_LENGTH,
            x + offset, y -offset, z -CUBE_LENGTH,
            // BACK QUAD
            x + offset, y -offset, z,
            x -offset, y -offset, z,
            x -offset, y + offset, z,
            x + offset, y + offset, z,
            // LEFT QUAD
            x -offset, y + offset, z -CUBE_LENGTH,
            x -offset, y + offset, z,
            x -offset, y -offset, z,
            x -offset, y -offset, z -CUBE_LENGTH,
            // RIGHT QUAD
            x + offset, y + offset, z,
            x + offset, y + offset, z -CUBE_LENGTH,
            x + offset, y -offset, z -CUBE_LENGTH,
            x + offset, y -offset, z };
    }
    
    // method: getCubeColor
    // purpose: This method returns a color for the cube.
    private float[] getCubeColor(Block block) 
    {
        return new float[] { 1, 1, 1 };
    }
    
    // method: createTexCube
    // purpose: This method creates the textures
    // (from the  terrian.png file) and adds the specified texture 
    //(depending on the case) onto all 6 sides of the block.
    public static float[] createTexCube(float x, float y, Block block) 
    {
        float offset = (1024f/16)/1024f;
        switch (block.getID())
        {
            case 0:
                return new float[] 
                {
                // BOTTOM QUAD(DOWN=+Y)
                x + offset*3, y + offset*10,
                x + offset*2, y + offset*10,
                x + offset*2, y + offset*9,
                x + offset*3, y + offset*9,
                // TOP!
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // FRONT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                // BACK QUAD
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                // LEFT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                // RIGHT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1};
            case 1:
                return new float[] 
                {
                // BOTTOM QUAD(DOWN=+Y)
                x + offset*3, y + offset*10,
                x + offset*2, y + offset*10,
                x + offset*2, y + offset*9,
                x + offset*3, y + offset*9,
                // TOP!
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // FRONT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                // BACK QUAD
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                // LEFT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                // RIGHT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1};
            case 3:
                return new float[] 
                {
                // BOTTOM QUAD(DOWN=+Y)
                x + offset*3, y + offset*10,
                x + offset*2, y + offset*10,
                x + offset*2, y + offset*9,
                x + offset*3, y + offset*9,
                // TOP!
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // FRONT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                // BACK QUAD
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                // LEFT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                // RIGHT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1};
            case 4:
                return new float[] 
                {
                // BOTTOM QUAD(DOWN=+Y)
                x + offset*3, y + offset*10,
                x + offset*2, y + offset*10,
                x + offset*2, y + offset*9,
                x + offset*3, y + offset*9,
                // TOP!
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // FRONT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                // BACK QUAD
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                // LEFT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                // RIGHT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1};
            case 5:
                return new float[] 
                {
                // BOTTOM QUAD(DOWN=+Y)
                x + offset*3, y + offset*10,
                x + offset*2, y + offset*10,
                x + offset*2, y + offset*9,
                x + offset*3, y + offset*9,
                // TOP!
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // FRONT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                // BACK QUAD
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                // LEFT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                // RIGHT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1};
        }
                return new float[] 
                {
                // BOTTOM QUAD(DOWN=+Y)
                x + offset*3, y + offset*10,
                x + offset*2, y + offset*10,
                x + offset*2, y + offset*9,
                x + offset*3, y + offset*9,
                // TOP!
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // FRONT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                // BACK QUAD
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                // LEFT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                // RIGHT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1};
    }
    
    // method: Chunk
    // purpose: This method is the constructor for our chunk class.
    public Chunk(int startX, int startY, int startZ)
    {
        try{texture = TextureLoader.getTexture("PNG",
            ResourceLoader.getResourceAsStream("res/terrain.png"));
        }
        
        catch(Exception e)
        {
            System.out.print("ER-ROAR!");
        }
        r = new Random();
        simplexNoise = new SimplexNoise(CHUNK_SIZE * 3 / 2, 0.175, r.nextInt());
        Blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
        for (int x = 0; x < CHUNK_SIZE; x++)
        {
            for (int y = 0; y < CHUNK_SIZE; y++)
            {
                for (int z = 0; z < CHUNK_SIZE; z++)
                {
                    float blockType = r.nextFloat();
                    if (blockType > 0.7f) {
                        Blocks[x][y][z] = new Block(BlockType.Grass);
                    } else if (blockType > 0.4f) {
                        Blocks[x][y][z] = new Block(BlockType.Dirt);
                    } else if (blockType > 0.2f) {
                        Blocks[x][y][z] = new Block(BlockType.Water);
                    } else {
                        //this is the default block type, right now set to dirt
                        Blocks[x][y][z] = new Block(BlockType.Dirt);
                    }
                    double heightNoise = Math.abs(simplexNoise.getNoise(x, z) * CHUNK_SIZE);
                    Blocks[x][y][z].setActive(heightNoise + CHUNK_SIZE / 2 >= y);
                }
            }
        }
        VBOColorHandle = glGenBuffers();
        VBOVertexHandle = glGenBuffers();
        VBOTextureHandle = glGenBuffers(); 
        StartX = startX;
        StartY = startY;
        StartZ = startZ;
        rebuildMesh(startX, startY, startZ);
    }
}


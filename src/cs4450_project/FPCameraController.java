/*
 * file: FPCameraController.java
 * author: I. Quintanilla, N. Baron, D. Faizi
 * class: CS 4450 - Computer Graphics
 *
 * assignment: Project
 * date last modified: 11/11/2021
 *
 * purpose: This class will hold our camera's position in 3D space and allow
 * the camera to move in different directions by keyboard input.
 */
package cs4450_project;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL11.*;

public class FPCameraController {

    private static final int CHUNK_RADIUS = 2;

    private final ArrayList<Chunk> chunks;

    private final Vector3f lPosition;

    // the rotation around the X axis of the camera
    private float pitch;
    // 3D vector to store the camera's position
    private final Vector3f position;
    // the rotation around the Y axis of the camera
    private float yaw;

    /*
     * method: constructor
     * purpose: initialize the FPCamera
     */
    public FPCameraController(float x, float y, float z)
    {
        position = new Vector3f(x, y, z);
        lPosition = new Vector3f(
            Chunk.CHUNK_SIZE / 2 * Chunk.CUBE_LENGTH,
            Chunk.CHUNK_SIZE * 2 * Chunk.CUBE_LENGTH,
            (Chunk.CHUNK_SIZE / 2 + 10) * Chunk.CUBE_LENGTH
        );
        //this.chunk = c;
        yaw = 0;
        pitch = 0;
        chunks = new ArrayList<>();
        regenerateChunks();
    }

    /*
     * method: gameLoop
     * purpose: handle main game logic, this includes movement controls.
     */
    public void gameLoop()
    {
        float dx = 0.0f;
        float dy = 0.0f;
        float dt = 0.0f; // length of a frame
        float lastTime = 0.0f; // when the last frame was
        long lastChunkRegen = 0;
        long time = 0;
        float mouseSensitivity = 0.09f;
        float movementSpeed = .35f;
        int chunksRemoved = 0;

        ArrayList<Chunk> toRemove = new ArrayList<>();

        // hide the mouse
        Mouse.setGrabbed(true);

        // keep looping till the display window is closed or ESC_KEY is pressed down
        while (!Display.isCloseRequested()
            && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            time = Sys.getTime();
            lastTime = time;

            // distance in mouse movement from last getDX() call
            dx = Mouse.getDX();
            // distance in mouse movement from last getDY() call
            dy = Mouse.getDY();

            // control camera yaw from x movement from the mouse
            yaw(dx * mouseSensitivity);
            // control camera pitch from y movement from the mouse
            pitch(dy * mouseSensitivity);

            //The following assigns keyboard controls to move the
            //camera in a specific direction:
            //move forward
            if (Keyboard.isKeyDown(Keyboard.KEY_W))
            {
                walkForward(movementSpeed);
            }
            //move backwards
            if (Keyboard.isKeyDown(Keyboard.KEY_S))
            {
                walkBackwards(movementSpeed);
            }
            //strafe left
            if (Keyboard.isKeyDown(Keyboard.KEY_A))
            {
                strafeLeft(movementSpeed);
            }
            //strafe right
            if (Keyboard.isKeyDown(Keyboard.KEY_D))
            {
                strafeRight(movementSpeed);
            }
            //moves up
            if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))
            {
                moveUp(movementSpeed);
            }
            //moves down
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
            {
                moveDown(movementSpeed);
            }
            // regenerate chunks
            if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
                regenerateChunks();
            }

            // set the modelview matrix back to the identity
            glLoadIdentity();
            // look through the camera before anything is drawn
            lookThrough();
            //handles light position
            FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
            lightPosition.put(lPosition.x).put(
            lPosition.y).put(lPosition.z).put(1.0f).flip();
            glLight(GL_LIGHT0, GL_POSITION, lightPosition);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            // draw scene here
            final int playerX = (int) Math.floor(position.x
                / Chunk.CUBE_LENGTH);
            final int playerZ = (int) Math.floor(position.z
                / Chunk.CUBE_LENGTH);
            for(Chunk chunk : chunks){
                chunk.render();
                if (chunk.getBlockDistance(playerX, playerZ)
                    > (CHUNK_RADIUS * Chunk.CHUNK_SIZE * 5) / 3)
                    toRemove.add(chunk);
            }
            // draw the buffer to the screen
            Display.update();

            if(!toRemove.isEmpty()){
                int oldChunkCount = chunks.size();
                chunks.removeAll(toRemove);
                toRemove.clear();
                chunksRemoved += oldChunkCount - chunks.size();

                if(chunksRemoved >= CHUNK_RADIUS * CHUNK_RADIUS
                    && System.currentTimeMillis() - lastChunkRegen > 2000){
                    // 3 chunks were lost.
                    // add some new ones
                    regenerateChunks();
                    lastChunkRegen = System.currentTimeMillis();
                    chunksRemoved = 0;
                }
            }

            Display.sync(60);
        }
        Display.destroy();
    }

    /*
     * method: lookThrough
     * purpose: translates and rotates the matrix
     * so that it looks though the camera
     */
    public void lookThrough() {
        // rotate the pitch around the X axis
        glRotatef(pitch, 1.0f, 0.0f, 0.0f);

        // rotate the yaw around the Y axis
        glRotatef(yaw, 0.0f, 1.0f, 0.0f);

        // translate to the position vector's location
        glTranslatef(position.x, position.y, position.z);
    }

    /*
     * method: moveDown
     * purpose: moves the camera down relative to its current rotation
     */
    public void moveDown(float distance)
    {
        position.y += distance;
    }

    /*
     * method: moveUp
     * purpose: moves the camera up relative to its current rotation (yaw)
     */
    public void moveUp(float distance)
    {
        position.y -= distance;
    }

    /*
     * method: pitch
     * purpose: increment the camera's current pitch rotation
     */
    public void pitch(float amount) {
        pitch -= amount;
    }

    // method: regenerateChunks
    // purpose: add new chunks relative to the current camera position
    private void regenerateChunks() {

        final int playerX = (int) Math.floor(position.x / Chunk.CHUNK_LENGTH);
        final int playerZ = (int) Math.floor(position.z / Chunk.CHUNK_LENGTH);
        for(int xOff = -CHUNK_RADIUS; xOff <= CHUNK_RADIUS; ++xOff){
            for (int zOff = -CHUNK_RADIUS; zOff <= CHUNK_RADIUS; ++zOff) {
                int xPosition = (xOff - playerX) * Chunk.CHUNK_SIZE;
                int zPosition = (zOff - playerZ) * Chunk.CHUNK_SIZE;

                if (chunks.stream().anyMatch(
                    chunk -> chunk.containsBlock(xPosition, zPosition))) {
                    continue;
                }

                chunks.add(new Chunk(xPosition, zPosition));
            }
        }
    }

    /*
     * method: strafeLeft
     * purpose: strafes the camera left relative to its current rotation (yaw)
     */
    public void strafeLeft(float distance)
    {
        float xOffset = distance * (float)Math.sin(Math.toRadians(yaw-90));
        float zOffset = distance * (float)Math.cos(Math.toRadians(yaw-90));
        position.x -= xOffset;
        position.z += zOffset;
    }

    /*
     * method: strafeRight
     * purpose: strafes the camera right relative to its current rotation (yaw)
     */
    public void strafeRight(float distance)
    {
        float xOffset = distance * (float)Math.sin(Math.toRadians(yaw+90));
        float zOffset = distance * (float)Math.cos(Math.toRadians(yaw+90));
        position.x -= xOffset;
        position.z += zOffset;
    }

    /*
     * method: walkBackwards
     * purpose: moves the camera backward relative to its current rotation (yaw)
     */
    public void walkBackwards(float distance)
    {
        float xOffset = distance * (float)Math.sin(Math.toRadians(yaw));
        float zOffset = distance * (float)Math.cos(Math.toRadians(yaw));
        position.x += xOffset;
        position.z -= zOffset;
    }

    /*
     * method: walkForward
     * purpose: This moves the camera forward relative to its current rotation
     */
    public void walkForward(float distance)
    {
        float xOffset = distance * (float)Math.sin(Math.toRadians(yaw));
        float zOffset = distance * (float)Math.cos(Math.toRadians(yaw));
        position.x -= xOffset;
        position.z += zOffset;
    }

    /*
     * method: yaw
     * purpose: increment the camera's current yaw rotation
     */
    public void yaw(float amount) {
        yaw += amount;
    }
}

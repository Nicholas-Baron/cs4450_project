/*
 * file: FPCameraController.java
 * author: Isaac Quintanilla
 * class: CS 4450 - Computer Graphics
 *
 * assignment: Project
 * date last modified: 10/9/2021
 *
 * purpose: This class will hold our camera's position in 3D space.
 */
package cs4450_project;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.Sys;

public class FPCameraController {
    // include CUBE class for gameloop render
    Cube cube = new Cube(0, 0, 0, 1);
    // 3D vector to store the camera's position
    private Vector3f position = null;
    private Vector3f lPosition = null;
    
    // the rotation around the Y axis of the camera
    private float yaw = 0.0f;
    
    // the rotation around the X axis of the camera
    private float pitch = 0.0f;
    
    private Vector3Float me;
    
    public FPCameraController(float x, float y, float z){
	// instantiate position Vector3f to the x y z parameters
	position = new Vector3f(x, y, z);
	lPosition = new Vector3f(x, y, z);
	lPosition.x =0f;
	lPosition.y = 15f;
	lPosition.z = 0f;
	
    }
    
    // increment the camera's current yaw rotation
    public void yaw(float amount){
	// increment the yaw by the amount parameter
	yaw+= amount;
    }
    
    // increment the camera's current pitch rotation
    public void pitch(float amount){
	// increment the pitch by the amount parameter
	pitch -= amount;
    }
    
    // translates and rotates the matrix so that it looks though the camera
    public void looThrough(){
	// rotate the pitch around the X axis
	glRotatef(pitch, 1.0f, 0.0f, 0.0f);
	
	// rotate the yaw around the Y axis
	glRotatef(yaw, 0.0f, 1.0f, 0.0f);
	
	// translate to the position vector's location
	glTranslatef(position.x, position.y, position.z);
    }
    
    public void gameLoop(){
	FPCameraController camera = new FPCameraController(0, 0, 0);
	float dx = 0.0f;
	float dy = 0.0f;
	float dt = 0.0f; // length of a frame
	float lastTime = 0.0f; // when the last frame was
	long time = 0;
	float mouseSensitivity = 0.09f;
	float movementSpeed = .35f;
	
	// hide the mouse
	Mouse.setGrabbed(true);
	
	// keep looping till the display window is closed or ESC_KEY is pressed down
	while(!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
	    time = Sys.getTime();
	    lastTime = time;
	    
	    // distance in mouse movement from last getDX() call
	    dx = Mouse.getDX();
	    // distance in mouse movement from last getDY() call
	    dy = Mouse.getDY();
	    
	    // control camera yaw from x movement from the mouse
	    camera.yaw(dx * mouseSensitivity);
	    // control camera pitch from y movement from the mouse
	    camera.pitch(dy * mouseSensitivity);
	    
	    // set the modelview matrix back to the identity
	    glLoadIdentity();
	    // look through the camera before anything is drawn
	    camera.looThrough();
	    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	    
	    // draw scene here
	    cube.draw();
	    
	    // draw the buffer to the screen
	    Display.update();
	    Display.sync(60);
	    
	}
	Display.destroy();
    }
}

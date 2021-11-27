/*
 * file: Main.java
 * author: N. Baron, I. Quintanilla, D. Faizi
 * class: CS 4450 - Computer Graphics
 *
 * assignment: Program 1
 * date last modified: 11/11/2021
 *
 * purpose: This program renders a chunk demo with lighting.
 * Pressing ESC will close the window.
 */
package cs4450_project;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.glu.GLU;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;

public final class Main {

    public static final int HEIGHT = 480;
    public static final int WIDTH = 640;

    private DisplayMode displayMode;
    private FPCameraController fp;
    private FloatBuffer lightPosition;
    private FloatBuffer whiteLight;


    /*
     * method: createWindow
     * purpose: Initialize the OpenGL display
     */
    private void createWindow() throws LWJGLException {
        Display.setFullscreen(false);
        for (DisplayMode d : Display.getAvailableDisplayModes()) {
            if (d.getWidth() == WIDTH
                && d.getHeight() == HEIGHT
                && d.getBitsPerPixel() == 32) {
                displayMode = d;
                break;
            }
        }
        if (displayMode == null) {
            displayMode = new DisplayMode(WIDTH, HEIGHT);
        }
        Display.setDisplayMode(displayMode);
        Display.setTitle("CS4450 Project");
        Display.create();
    }

    /*
     * method: initOpenGL
     * purpose: Initialize the rest of OpenGL once the display is opened.
     */
    private void initGL() {
        //used for transparency, for flowers
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClearDepth(1);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glMatrixMode(GL_PROJECTION);
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        
        initLightArrays();
        //set light source position
        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
        //sets specular light
        glLight(GL_LIGHT0, GL_SPECULAR, whiteLight);
        //sets diffuse light
        glLight(GL_LIGHT0, GL_DIFFUSE, whiteLight);
        //sets ambient light
        glLight(GL_LIGHT0, GL_AMBIENT, whiteLight);
        //this enables our lighting
        glEnable(GL_LIGHTING);
        //enables light0
        glEnable(GL_LIGHT0);
        
        glLoadIdentity();
        GLU.gluPerspective(100.0f,
            displayMode.getWidth() / (float) displayMode.getHeight(),
            0.1f,
            300.0f);
        glMatrixMode(GL_MODELVIEW);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
    }

    /**
     * method: start
     * purpose: start each component of the program.
     */
    private void start() 
    {
        
        try {
            createWindow();
            initGL();

            fp = new FPCameraController(0f, 0f, 0f); 
            fp.gameLoop();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * method: initLightArrays
     * purpose: This method is the controller of our light source
     * within our program. Also, determines the position of the light source.
     */
    private void initLightArrays() {
        //keeps track of light source location.
        lightPosition = BufferUtils.createFloatBuffer(4);
        //light source position
        lightPosition.put(0.0f).put(0.0f).put(0.0f).put(1.0f).flip();
        // Indicate the RGB values of our light source.
        whiteLight = BufferUtils.createFloatBuffer(4);
        whiteLight.put(1.0f).put(1.0f).put(1.0f).put(0.0f).flip();
    }

    /**
     * method: main
     * purpose: run the whole program
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main main = new Main();
        main.start();
    }

}

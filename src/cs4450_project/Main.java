/*
 * file: Main.java
 * author: N. Baron, I. Quintanilla
 * class: CS 4450 - Computer Graphics
 *
 * assignment: Program 1
 * date last modified: 10/9/2021
 *
 * purpose: This program renders a cube demo.
 * Pressing ESC will close the window.
 */
package cs4450_project;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.glu.GLU;

import static org.lwjgl.opengl.GL11.*;

public final class Main {

    public static final int HEIGHT = 480;
    public static final int WIDTH = 640;

    private DisplayMode displayMode;
    private final FPCameraController fp;

    /*
     * constructor
     * purpose: initialize instance variables
     * and ensure only this class can construct itself.
     */
    private Main() {
        fp = new FPCameraController(0f, 0f, 0f);
    }

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
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClearDepth(1);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glMatrixMode(GL_PROJECTION);
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
    private void start() {
        try {
            createWindow();
            initGL();
            fp.gameLoop();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

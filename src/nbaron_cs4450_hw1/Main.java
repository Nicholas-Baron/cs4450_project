/*
 * file: Main.java
 * author: N. Baron
 * class: CS 4450 - Computer Graphics
 *
 * assignment: Program 1
 * date last modified: 8/23/2021
 *
 * purpose: This program reads a coordinates.txt file and renders it in LWJGL.
 * It uses glVertex2f to render each shape point-by-point.
 * Pressing ESC will close the window.
 */
package nbaron_cs4450_hw1;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import static org.lwjgl.opengl.GL11.*;

public final class Main {

    /*
     * method: createWindow
     * purpose: Initialize the OpenGL display
     */
    private void createWindow() throws LWJGLException {
        Display.setFullscreen(false);
        Display.setDisplayMode(new DisplayMode(640, 480));
        Display.setTitle("Test OpenGL");
        Display.create();
    }

    /*
     * method: initOpenGL
     * purpose: Initialize the rest of OpenGL once the display is opened.
     */
    private void initOpenGL() {
        glClearColor(0, 0, 0, 0);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, 640, 0, 480, 1, -1);
        glMatrixMode(GL_MODELVIEW);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
    }

    /*
     * method: render
     * purpose: Draw to the display until it closes
     */
    private void render() {
        while (!Display.isCloseRequested()) try {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glLoadIdentity();

            glColor3f(1, 1, 0);
            glPointSize(10);

            glBegin(GL_POINTS);
            {
                glVertex2f(350, 150);
                glVertex2f(50, 50);
            }
            glEnd();

            Display.update();
        } catch (Exception e) {
        }

        Display.destroy();
    }

    /**
     * method: start
     * purpose: start each component of the program.
     */
    private void start() {
        try {
            createWindow();
            initOpenGL();
            render();
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

package com.peregrine.test;

import static java.awt.Desktop.getDesktop;
import static java.awt.Desktop.isDesktopSupported;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.write;

import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.command.Input;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import static io.webfolder.cdp.type.constant.MouseButtonType.Left;
import static io.webfolder.cdp.type.constant.MouseEventType.MousePressed;
import static io.webfolder.cdp.type.constant.MouseEventType.MouseReleased;
/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException, InterruptedException
    {
        SessionFactory factory = new Launcher().launch();


        try (Session session = factory.create()) {

//            session.getCommand().getBrowser().getWindowBounds(session.);

            session.navigate("http://localhost:8080");
            session.enableConsoleLog();
            session.installSizzle();
            session.enableNetworkLog();
//            session.getCommand().getEmulation().setVisibleSize(800,600);
            session.waitDocumentReady();
            makeScreenshot(session, "home.png");

//            Thread.sleep(1000);

            waitThenClick(session, "a:contains('admin')");
            makeScreenshot(session, "adminhome.png");
            waitThenClick(session, "a:contains('explore')");
            makeScreenshot(session, "pageexplore.png");
            waitThenClick(session, "a:eq(10)");

            session.wait(1000);
            int left = (int) 100;
            int  top = (int) 200;
            int clickCount = 1;
            Input input = session.getCommand().getInput();
            input.dispatchMouseEvent(MousePressed, left, top, null, null, Left, clickCount);
            input.dispatchMouseEvent(MouseReleased, left, top, null, null, Left, clickCount);

            makeScreenshot(session, "final.png");
        }

//        if (isDesktopSupported()) {
//            getDesktop().open(file.toFile());
//        }

        factory.close();
    }

    private static void makeScreenshot(Session session, String name) throws InterruptedException, IOException {
        session.getCommand().getEmulation().forceViewport(0.,0.,1.0);
        Thread.sleep(2000);
        Path file = FileSystems.getDefault().getPath("./target/",name);
        byte[] data = session.captureScreenshot();
        write(file, data);
    }

    private static void waitThenClick(Session session, String clickOn) {
        boolean succeed = session.waitUntil(s -> { return s.matches(clickOn); }, 1000);
        session.click(clickOn);
    }
}

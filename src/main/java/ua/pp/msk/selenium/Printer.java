/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.pp.msk.selenium;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Set;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.MarionetteDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author maskimko
 */
public class Printer {

    private static final int QN = 138;
    public static final long TIMEOUT = 20000;
    private Thread printThread;
    private WebDriver driver;
    private   Console console = System.console();
    private Credentials creds;
    private static class Credentials {
        String login;
        String password;
    }

    public static void main(String[] args) {
        
        //String url = "https://www.selftestsoftware.com/default.aspx";
        Printer m = new Printer();
        try {
           // m.dump("/home/maskimko/Documents/java8TrainingDump", url);
           m.creds = m.askCredentials();
           m.dump(m.askOutputDirectory(), m.askUrl());
        } catch (MalformedURLException ex) {
            System.err.println("Bad url " + ex.getMessage());
        }
    }
    
    private URL askUrl() throws MalformedURLException{
        System.out.println("Input the target URL: ");
        String readLine = console.readLine();
        URL url = new URL(readLine);
        return url;
    }
    private File askOutputDirectory() {
        System.out.println("Input the path to the output directory: ");
        String readLine = console.readLine();
        File outDir = new File(readLine);
        return outDir;
    }
    private Credentials askCredentials(){
        System.out.println("Input your login: ");
        String login = console.readLine();
        System.out.println("Input your password: ");
        char[] pass = console.readPassword();
        Credentials c = new Credentials();
        c.login = login;
        c.password = new String(pass);
        return c;
    }

    public void dump(String directory, String selfTestUrl) throws MalformedURLException {
        File dir = null;
        if (directory != null && directory.length() > 0) {
            dir = new File(directory);
        }
        URL url = new URL(selfTestUrl);
        dump(dir, url);
    }

    public void dump(File directory, URL selfTestUrl) {
        FirefoxProfile prof = new FirefoxProfile();
        //prof.setPreference("browser.startup.homepage_override.mstone", "ignore");
        //prof.setPreference("startup.homepage_welcome_url.additional", "about:blank");
        prof.setPreference("print.print_to_file", true);
        prof.setPreference("print.always_print_silent", true);
        System.setProperty("webdriver.gecko.driver", "/opt/server/Selenium/bin/geckodriver-0.8.0-linux64");
        DesiredCapabilities caps = DesiredCapabilities.firefox();
        caps.setCapability("marionette", true);
        // caps.setCapability(FirefoxDriver.PROFILE, prof);
        Robot rb = null;
        try {
            rb = new Robot();
            driver = new MarionetteDriver(caps);
            //driver.get(selfTestUrl.toString());
            driver.navigate().to(selfTestUrl);
            //WebDriverWait wait = new WebDriverWait(driver, 30);
            // wait.until(ExpectedConditions.elementToBeClickable(By.className("login"))).click();
            WebElement loginButton = driver.findElement(By.className("login"));
            System.out.println(loginButton.getText());
            String loginPage = loginButton.getAttribute("href");
            driver.navigate().to(loginPage);
            WebElement loginInput = driver.findElement(By.name("DAFLOGIN"));
            WebElement passwordInput = driver.findElement(By.name("DAFPASS"));
            loginInput.sendKeys(creds.login);
            passwordInput.sendKeys(creds.password);
            driver.findElement(By.name("BtnSubmit")).submit();
            WebDriverWait wait = new WebDriverWait(driver, 20);
            WebElement productListLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("View My Products")));
            String myProductsLink = productListLink.getAttribute("href");
//                wait.until(ExpectedConditions.elementToBeClickable(productListLink)).click();
            driver.navigate().to(myProductsLink);
            WebElement activationButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("cmdActivateOnline")));
            String parentWindowHandler = driver.getWindowHandle();
            wait.until(ExpectedConditions.elementToBeClickable(activationButton)).click();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            Iterator<String> windowHandlerIter = driver.getWindowHandles().iterator();
            while (windowHandlerIter.hasNext()) {
                String currentHandler = windowHandlerIter.next();
                if (!currentHandler.equals(parentWindowHandler)) {
                    driver.switchTo().window(currentHandler);

                    rb.delay(15000);
                    rb.keyPress(KeyEvent.VK_TAB);
                    rb.keyRelease(KeyEvent.VK_TAB);
                    rb.keyPress(KeyEvent.VK_TAB);
                    rb.keyRelease(KeyEvent.VK_TAB);
                    rb.keyPress(KeyEvent.VK_ENTER);
                    rb.keyRelease(KeyEvent.VK_ENTER);
                    rb.keyPress(KeyEvent.VK_DOWN);
                    rb.keyRelease(KeyEvent.VK_DOWN);
                    rb.keyPress(KeyEvent.VK_ENTER);
                    rb.keyRelease(KeyEvent.VK_ENTER);
                    System.out.println("Current URL: " + driver.getCurrentUrl());
                    System.out.println("Title : " + driver.getTitle());
                } else {
                    System.out.println("Skipping the parent window");
                }
            }
            Thread.sleep(10000);
            Set<String> windowHandles = driver.getWindowHandles();
            for (String currentWindowHandle : windowHandles) {
                driver.switchTo().window(currentWindowHandle);
                System.out.println("Current URL: " + driver.getCurrentUrl());
                System.out.println("Title : " + driver.getTitle());
                if (driver.getTitle().equals("Self Test -1Z0-810 - Upgrade Java SE 7 to Java SE 8 OCP Programmer")) {
                    wait.until(ExpectedConditions.elementToBeClickable(By.id("choose_mode.LearnButton"))).click();
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Item_Selection_ItemsText"))).clear();
                    driver.findElement(By.id("Item_Selection_ItemsText")).sendKeys("" + QN);
                    WebElement startTestButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Item_Selection.StartTestLink")));
                    wait.until(ExpectedConditions.elementToBeClickable(startTestButton)).click();

                    //Learning curve
                    for (int i = 1; i <= QN; i++) {
                        System.out.println("Processing question " + i);
                        Thread.sleep(3000);
                        WebElement learnNowButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Item.LearnNowLink")));

                        wait.until(ExpectedConditions.elementToBeClickable(learnNowButton)).click();
                        System.err.println("Learn now");

                        printThread = new Thread(() -> {

                            try {
                                System.err.println("In The print thread");
                                Thread.sleep(1000);
                                Robot robot = new Robot();
                                robot.delay(3000);
                                robot.mouseMove(397, 277);
                                System.err.println("Mouse PDF printer position");
                                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                                System.err.println("Mouse clicked");
                                robot.mouseMove(954, 164);
                                System.err.println("Mouse on print position");
                                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                                System.err.println("Mouse clicked");
                                Thread.sleep(4000);
                            } catch (AWTException ex) {
                                System.err.println("Robot exception " + ex.getMessage());
                            } catch (InterruptedException ex) {
                                System.err.println("Robot has been interrupted " + ex.getMessage());
                            }

                        });
                        Thread.sleep(7000);
                        printThread.start();
                        WebElement printLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Item.PrintLink")));
                        wait.until(ExpectedConditions.elementToBeClickable(printLink)).click();

                        //COpy files
                        Thread.sleep(5000);
                        if (directory != null && directory.isDirectory() && directory.canWrite()) {
                            try {
                                Files.move(Paths.get("/home/maskimko/NetBeansProjects/SeleniumTryMaven/mozilla.pdf"),
                                        Paths.get(directory.getAbsolutePath(), String.format("Question%d.pdf", i)),
                                        StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException ex) {
                                System.err.println("Cannot move the file " + ex.getMessage());
                            }
                        } else {
                            System.err.println("Cannot write to directory ");
                        }

                        WebElement lowNextButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Item.BNextLink")));
                        wait.until(ExpectedConditions.elementToBeClickable(lowNextButton)).click();
                        System.err.println("Next page");
                    }
                    WebElement endTestButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Item.EndTestLink")));
                        wait.until(ExpectedConditions.elementToBeClickable(endTestButton)).click();
                        //Separate window
                         WebElement endTestOkButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("endtest.OK")));
                        wait.until(ExpectedConditions.elementToBeClickable(endTestOkButton)).click();
                        //Separate window end
                          WebElement exitTestButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Score.ExitTestLink")));
                        wait.until(ExpectedConditions.elementToBeClickable(exitTestButton)).click();
                }
            }
           
        } catch (AWTException ex) {
            System.err.println("Error " + ex.getMessage());
        } catch (InterruptedException ex) {
            System.err.println("Interrupted " + ex.getMessage());
        } finally {
            if (driver != null) {
                System.out.println("End of program");
                // driver.quit();
            }
        }
    }

}

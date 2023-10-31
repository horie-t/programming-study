package org.example;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    public void testPath()
    {
        assertTrue(Arrays.asList(new Cell(0, 0)).contains(new Cell(0, 0)));
    }

    public void testPicture()
    {
        assertTrue(new HashSet<Picture>(Arrays.asList(Picture.GINKGO, Picture.PALETTE)).contains(Picture.PALETTE));
    }

    public void testPictureNotContains()
    {
        assertFalse(new HashSet<Picture>(Arrays.asList(Picture.GINKGO, Picture.PALETTE)).contains(Picture.WINE));
    }
}

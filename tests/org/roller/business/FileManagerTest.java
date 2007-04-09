package org.roller.business;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.roller.model.FileManager;
import org.roller.model.PropertiesManager;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.UserManager;
import org.roller.pojos.RollerPropertyData;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.util.RollerMessages;

/**
 * @author David M Johnson
 */
public class FileManagerTest extends TestCase
{
    //private Roller mRoller = null;
    //private WebsiteData mWebsite = null;
    
    public void setUp() throws Exception
    {
        
    }
    
    public void tearDown() throws Exception
    {
        // No need to clean up, we are using mocks
    }
    
    public void testCanSave() 
    {
        try 
        {
            // do some setup for our test
            Roller mRoller = RollerFactory.getRoller();
            
            UserManager umgr = mRoller.getUserManager();
            WebsiteData mWebsite = umgr.getWebsite("FileManagerTest_userName");
            
            if(mWebsite == null)
                mWebsite = this.createTestUser();
            
            // update roller properties to prepare for test
            PropertiesManager pmgr = mRoller.getPropertiesManager();
            Map config = pmgr.getProperties();
            ((RollerPropertyData)config.get("uploads.enabled")).setValue("false");
            ((RollerPropertyData)config.get("uploads.types.forbid")).setValue("gif");
            ((RollerPropertyData)config.get("uploads.dir.maxsize")).setValue("1.00");
            pmgr.store(config);
            mRoller.commit();
            
            FileManager fmgr = new FileManagerImpl();
            RollerMessages msgs = new RollerMessages();
            assertFalse(fmgr.canSave(mWebsite, "test.gif", 2500000, msgs));
            //assertEquals(4, msgs.getErrorCount());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void testSave()
    {
        try 
        {
            // do some setup for our test
            Roller mRoller = RollerFactory.getRoller();
            
            UserManager umgr = mRoller.getUserManager();
            WebsiteData mWebsite = umgr.getWebsite("FileManagerTest_userName");
            
            if(mWebsite == null)
                mWebsite = this.createTestUser();
            
            // update roller properties to prepare for test
            PropertiesManager pmgr = mRoller.getPropertiesManager();
            Map config = pmgr.getProperties();
            ((RollerPropertyData)config.get("uploads.enabled")).setValue("true");
            ((RollerPropertyData)config.get("uploads.types.allowed")).setValue("opml");
            ((RollerPropertyData)config.get("uploads.dir.maxsize")).setValue("1.00");
            pmgr.store(config);
            mRoller.commit();
            
            /* NOTE: upload dir for unit tests is set in 
               roller/personal/testing/roller-custom.properties */
            FileManager fmgr = new FileManagerImpl();
            RollerMessages msgs = new RollerMessages();
            InputStream is = getClass().getResourceAsStream("/bookmarks.opml");
            fmgr.saveFile(mWebsite, "bookmarks.opml", 1545, is);
            
            assertEquals(1, fmgr.getFiles(mWebsite).length);
            
            fmgr.deleteFile(mWebsite, "bookmarks.opml");
            Thread.sleep(2000);
            assertEquals(0, fmgr.getFiles(mWebsite).length);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private WebsiteData createTestUser() throws Exception {
        // do some setup for our test
        Roller mRoller = RollerFactory.getRoller();
        
        UserManager umgr = mRoller.getUserManager();
        mRoller.begin(UserData.SYSTEM_USER);
        UserData user = new UserData(null,
                "FileManagerTest_userName",  
                "FileManagerTest_password",       
                "FileManagerTest_description",      
                "FileManagerTest@example.com", 
                new java.util.Date());
        Map pages = new HashMap();
        pages.put("Weblog","Weblog page content");
        pages.put("_day","Day page content");
        pages.put("css","CSS page content");
        umgr.addUser(user, pages, "basic", "en_US_WIN", "America/Los_Angeles");
        mRoller.commit();
        WebsiteData mWebsite = umgr.getWebsite(user.getUserName());
        
        return mWebsite;
    }

    public static Test suite()
    {
        return new TestSuite(FileManagerTest.class);
    }
}
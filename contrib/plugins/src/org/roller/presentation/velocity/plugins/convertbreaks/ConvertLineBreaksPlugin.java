/*
 * ConvertLineBreaksPlugin.java
 *
 * Created on July 10, 2005, 3:17 PM
 */

package org.roller.presentation.velocity.plugins.convertbreaks;

import java.io.BufferedReader;
import java.io.StringReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.context.Context;
import org.roller.RollerException;
import org.roller.pojos.WeblogEntryData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.velocity.PagePlugin;

/**
 * Simple page plugin that converts paragraphs of plain text into html paragraphs.
 * We wrap each full paragraph in html &lt;p&gt; opening and closing tags, and
 * also add &lt;br&gt; tags to the end of lines with breaks inside a paragraph.
 *
 * Example:
 * This is one
 * paragraph
 *
 * Becomes:
 * &lt;p&gt;This is one&lt;br/&gt;
 * paragraph&lt;/p&gt;
 *
 * @author Allen Gilliland
 */
public class ConvertLineBreaksPlugin implements PagePlugin {
    
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(ConvertLineBreaksPlugin.class);
    
    private static final String name = "Convert Line Breaks";
    private static final String description = "Convert plain text paragraphs to html by adding p and br tags";
    private static final String version = "0.1";
    
    
    public ConvertLineBreaksPlugin() {
        mLogger.debug("Instantiating ConvertLineBreaksPlugin v"+this.version);
    }

    
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
    
    public void init(RollerRequest rreq, Context ctx) throws RollerException {
        // we don't need to do any init.
        mLogger.debug("initing");
    }
    
    
    /**
     * Transform the given plain text into html text by inserting p and br
     * tags around paragraphs and after line breaks.
     */
    public String render(WeblogEntryData entry, boolean skip) {
        
        mLogger.debug("Rendering weblog entry: "+entry.getTitle());
        
        return this.render(entry.getText());
    }
    
    
    /**
     * Transform the given plain text into html text by inserting p and br
     * tags around paragraphs and after line breaks.
     */
    public String render(String str) {
        
        if(str == null || str.trim().equals(""))
            return "";
        
        mLogger.debug("Rendering string of length "+str.length());
        
        /* setup a buffered reader and iterate through each line
         * inserting html as needed
         *
         * NOTE: we consider a paragraph to be 2 endlines with no text between them
         */
        StringBuffer buf = new StringBuffer();
        try {
            BufferedReader br = new BufferedReader(new StringReader(str));
            
            String line = null;
            boolean insidePara = false;
            while((line = br.readLine()) != null) {
                
                if(!insidePara && line.trim().length() > 0) {
                    // start of a new paragraph
                    buf.append("\n<p>");
                    buf.append(line);
                    insidePara = true;
                } else if(insidePara && line.trim().length() > 0) {
                    // another line in an existing paragraph
                    buf.append("<br/>\n");
                    buf.append(line);
                } else if(insidePara && line.trim().length() < 1) {
                    // end of a paragraph
                    buf.append("</p>\n\n");
                    insidePara = false;
                }
            }
            
            // if the text ends without an empty line then we need to
            // terminate the last paragraph now
            if(insidePara)
                buf.append("</p>\n\n");
            
        } catch(Exception e) {
            mLogger.warn("trouble rendering text.", e);
            return str;
        }
        
        return buf.toString();
    }
}
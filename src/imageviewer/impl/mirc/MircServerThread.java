/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.impl.mirc;

/**
 * This class instantiates a MircServer, sends the query, and returns the result.
 */
 
public class MircServerThread

{

    public MircServerThread ()  {
    }

    /**
     * @param url The server URL.
     * @param query The query conforming to the mirc query schema.
     * @return The result string conforming to the mirc result schema.
     */

    public String sendQuery (String url, String query)  {

      MircServerLite Server =
        new MircServerLite (
			    //"http://mirc.rsna.org/mircstorage/service",
			    //"http://mirc.childrensmemorial.org/Boards/service",
			    url,
		       "TestServer",
		       query,
		       "1",
                       "");
      Server.start();

      boolean gotResult = false;
    while (!gotResult)  {

	gotResult = Server.ready;

    }
    //System.out.println (Server.result);

    return Server.result;

    }
    
			
}

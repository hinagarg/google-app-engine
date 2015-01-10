import java.util.Collections;
import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.BlobInfo;

import java.util.Map;
import java.util.List;
import java.util.Arrays;


@SuppressWarnings("serial")
public class CacheServlet extends HttpServlet {
																																					
	//For inserting small files in the MemCache (Upload)
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		String filename = req.getParameter("filename");
		String filesizeString = req.getParameter("filesize");
		int filesize = Integer.parseInt(filesizeString);

		//Step 1: Add the file name to the list of cached files in MemCache for later retrieval and listing
		//Filenames are stored in memcache as the value with the key of "cachedFiles"
		String key = "cachedFiles";
		String filenames = filename;		    
		Cache cache;
		CacheFactory cacheFactory;
		try {
			cacheFactory = CacheManager.getInstance().getCacheFactory();
			cache = cacheFactory.createCache(Collections.emptyMap());
			byte[] storedData = (byte[]) cache.get("cachedFiles");
			//Let all the filenames of the "cached files" store as a value of the key 'cachedFiles'
			//We used '/' as a separator between the filenames because this character 
			//can never be used as part of the filename
			if(storedData!=null){
				String storedFiles = new String(storedData);
				String[] names = storedFiles.split("//");
				if(!Arrays.asList(names).contains(filename)){  //To prevent storing duplicate keys
					filenames = filename + "//" + storedFiles;
				}else{
					filenames = storedFiles;
				}
			}
			// Insert Updated "cached file" to the Memcache
			byte[] value = filenames.getBytes();
			cache.put(key, value);
		} catch (CacheException e) {
			e.printStackTrace();
		}

		//Step 2: Store the content of the file in memcache as a value with its 'filename' as the key
		key = filename;
		//Read the contents of the file and store in buffer
		byte[] buffer = new byte[filesize];
		req.getInputStream().read(buffer);

		try{		
			cacheFactory = CacheManager.getInstance().getCacheFactory();
			cache = cacheFactory.createCache(Collections.emptyMap());
			//Store file in memcache
			cache.put(key, buffer);
		}
		catch(CacheException e){
			e.printStackTrace();
		}
				

		resp.sendRedirect("/index.jsp");
	}


	//For downloading an object(file) from Memcache
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String filename = req.getParameter("filename");
		Cache cache;
		CacheFactory cacheFactory;
		try {
			cacheFactory = CacheManager.getInstance().getCacheFactory();
			cache = cacheFactory.createCache(Collections.emptyMap());
			byte[] fileData = (byte[]) cache.get(filename);

			resp.getOutputStream().write(fileData);

		} catch (CacheException e) {
			e.printStackTrace();
		}
	}


}

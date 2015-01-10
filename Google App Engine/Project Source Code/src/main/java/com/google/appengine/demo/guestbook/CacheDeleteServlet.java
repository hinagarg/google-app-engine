import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;

import java.util.Map;
import java.util.List;
import java.util.Arrays;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Collections;
import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;


@SuppressWarnings("serial")
public class CacheDeleteServlet extends HttpServlet {
																																					
	private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
		.initialRetryDelayMillis(10)
		.retryMaxAttempts(10)
		.totalRetryPeriodMillis(15000)
		.build());
		
	//For removing an object(file) from memcache
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String filename = req.getParameter("filename");

		Cache cache;
		CacheFactory cacheFactory;
		try {
			cacheFactory = CacheManager.getInstance().getCacheFactory();
			cache = cacheFactory.createCache(Collections.emptyMap());
			String newNames = "";
			
			byte[] storedData = (byte[]) cache.get("cachedFiles");
			String storedFiles = new String(storedData);			
			String[] names = storedFiles.split("//");
			for(int i=0; i<names.length; i++){
				if(!names[i].equals(filename)){
					newNames = names[i] + "//" + newNames;
				}
			}
			
			cache.put("cachedFiles", newNames.getBytes());

		} catch (CacheException e) {
			e.printStackTrace();
		}

		resp.sendRedirect("/index.jsp");
	}


}

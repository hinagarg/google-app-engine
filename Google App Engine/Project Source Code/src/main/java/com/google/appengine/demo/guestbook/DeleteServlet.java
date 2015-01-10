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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;



import com.google.apphosting.api.ApiProxy;
import java.util.concurrent.ThreadFactory;
import com.google.appengine.api.ThreadManager;
import java.util.List;
import java.util.ArrayList;


@SuppressWarnings("serial")
public class DeleteServlet extends HttpServlet {
																																					
	public void init() {	 
		ServletContext sc = getServletContext();	 
		List timerList = new ArrayList();
		timerList.add(0l);
		sc.setAttribute("totalUploadTime",timerList);
		sc.setAttribute("refreshCount",1);
    }
	
	private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
		.initialRetryDelayMillis(10)
		.retryMaxAttempts(10)
		.totalRetryPeriodMillis(15000)
		.build());
		
	//For removing an object(file) from google cloud storage
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if(req.getParameter("eraserTimerList")!=null){
			ServletContext sc = getServletContext();			
			List timerList = new ArrayList();
			timerList.add(0l);
			sc.setAttribute("totalUploadTime",timerList);
		}
		else{
			if(true){
				String filename = req.getParameter("filename");
				deleteFile(filename);
				resp.sendRedirect("/index.jsp");
			}
			else{			
			final String filename = req.getParameter("filename");			
			Thread thread = ThreadManager.createBackgroundThread(new Runnable() {
			@Override
			public void run(){
				try{
					
					DeleteServlet.this.deleteFile(filename);
				}
				catch(IOException e){
		
				}
			}});			
			thread.start();
			resp.sendRedirect("/index.jsp");
			}
		}
	}
	
	public void deleteFile(String filename) throws IOException{
			long startTime = System.currentTimeMillis();
			GcsFilename gcsFilename = new GcsFilename("bgvbucket", filename);
			gcsService.delete(gcsFilename);
			long endTime = System.currentTimeMillis();
			ServletContext sc = getServletContext();
			List timerList = (ArrayList) sc.getAttribute("totalUploadTime");							
			timerList.add((endTime - startTime));			
			sc.setAttribute("totalUploadTime",timerList);
			sc.setAttribute("refreshCount",1);
	}


}

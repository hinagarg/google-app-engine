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
import java.nio.ByteBuffer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.google.appengine.api.blobstore.BlobInfo;
import java.util.Map;
import java.util.List;

public class UploadLargeServlet extends HttpServlet {
    
	private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

	private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
		.initialRetryDelayMillis(10)
		.retryMaxAttempts(10)
		.totalRetryPeriodMillis(15000)
		.build());

	private static final int BUFFER_SIZE = 2 * 1024 * 1024;

	// Upload File with Blobstore API (Server Side)
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {

		Map<String, List<BlobInfo>> blobsData = blobstoreService.getBlobInfos(req);
		for (String key : blobsData.keySet()){
			for(BlobInfo blob:blobsData.get(key)){
				BlobstoreInputStream in = new BlobstoreInputStream(blob.getBlobKey());

				GcsFilename gcsFilename = new GcsFilename("bgvbucket", blob.getFilename());
				GcsOutputChannel outputChannel = gcsService.createOrReplace(gcsFilename, GcsFileOptions.getDefaultInstance());
				copy(in, Channels.newOutputStream(outputChannel));

			}
		}

		res.sendRedirect("/");

	}


	// Transfer the data from the inputStream to the outputStream. Then close both streams.
	private void copy(InputStream input, OutputStream output) throws IOException {
		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = input.read(buffer);
			while (bytesRead != -1) {
				output.write(buffer, 0, bytesRead);
				bytesRead = input.read(buffer);
			}
		} finally {
			input.close();
			output.close();
		}
	}
}


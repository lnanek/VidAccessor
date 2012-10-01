package name.nanek.vidaccessor.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class RatingServlet extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGetOrPost(req, resp);	
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGetOrPost(req, resp);
	}
	
	private void doGetOrPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		String[] videoIds = req.getParameterValues("videoId");
		String ratingString = req.getParameter("rating");
		int rating;
		try {
			rating = Integer.parseInt(ratingString);
		} catch (NumberFormatException e) {
			writeResponse(resp, 0);
			return;
		}
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
	    int totalRating = 0;
		Transaction tx = null;
		try {
			//was an optimization not to use transactions when reading, but sometimes didn't get updated value when checking channel stats right after voting
			//if ( 0 != rating ) {
				tx = pm.currentTransaction();
				tx.begin();
			//}
			
			for( String videoId : videoIds ) {
				totalRating += updateVideo(videoId, rating, pm, tx);
			}
			
			if ( null != tx ) {
				tx.commit();
			}
		} finally {
			try {
	            if ( null != tx && tx.isActive()) {
	                tx.rollback();
	            }
			} finally {
				pm.close();
			}
		}

		writeResponse(resp, totalRating);
	}

	private int updateVideo(String videoId, int rating, PersistenceManager pm, Transaction tx) {
		//javax.jdo.Query query = pm.newQuery("select from VideoRating " +
		 //       "where videoId == videoIdParam " +
		 //       "parameters String videoIdParam ");
		
		Query query = pm.newQuery(VideoRating.class);
		query.setFilter("videoId == videoIdParam");
		query.declareParameters("String videoIdParam");
		int updateRating = 0;
		
		List<VideoRating> results = (List<VideoRating>) query.execute(videoId);
		
		if ( results.isEmpty() ) {
			//Don't make an entry for a read with no existing entry.
			if ( 0 != rating ) {
				//Make new entry.
				VideoRating videoRating = new VideoRating(videoId, rating);
				pm.makePersistent(videoRating);
				updateRating = rating;
			}
		} else {
			//Update entry.
			VideoRating videoRating = results.get(0);
			updateRating = videoRating.getRating() + rating;
			videoRating.setRating(updateRating);
		}
		
		return updateRating;
	}

	private void writeResponse(HttpServletResponse resp, int updateRating) throws IOException {
		resp.setContentType("text/plain");
		PrintWriter writer = resp.getWriter();
		writer.write("" + updateRating);
		writer.flush();
		writer.close();
		resp.flushBuffer();
	}
	
	
}

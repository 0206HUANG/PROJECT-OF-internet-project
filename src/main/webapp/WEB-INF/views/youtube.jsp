<%-- youtube.jsp --%>
<%@ include file="header.jsp" %>
<style>
   /* Inline CSS for youtube.jsp */
   .youtube-container {
       max-width: 1200px;
       margin: 20px auto;
       padding: 20px;
       background-color: #f9f9f9;
       border-radius: 8px;
       box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
       font-family: Arial, sans-serif;
   }
   .youtube-container h1 {
       text-align: center;
       color: #003366;
       margin-bottom: 20px;
   }
   .youtube-container h2 {
       color: #003366;
       margin: 30px 0 15px;
   }
   .search-form {
       display: flex;
       flex-wrap: wrap;
       gap: 10px;
   }
   .search-form input[type="text"] {
       flex: 1;
       min-width: 200px;
   }
   .search-form input[type="text"],
   .search-form select,
   .yt-form input[type="text"],
   .yt-form select,
   .yt-form textarea {
       padding: 10px;
       font-size: 1rem;
       border: 1px solid #ccc;
       border-radius: 5px;
       box-sizing: border-box;
   }
   .btn {
       padding: 10px 20px;
       font-size: 1rem;
       color: white;
       background-color: #cc0000;
       border: none;
       border-radius: 5px;
       cursor: pointer;
       text-decoration: none;
       transition: background-color 0.3s;
   }
   .btn:hover {
       background-color: #e62117;
   }
   .btn-secondary {
       background-color: #003366;
   }
   .btn-secondary:hover {
       background-color: #0055a5;
   }
   .notice {
       padding: 12px 15px;
       background-color: #fff8e1;
       border: 1px solid #ffe08a;
       border-radius: 5px;
       color: #6b5300;
   }
   .preview {
       display: none;
       max-width: 800px;
       margin: 20px auto 0;
   }
   .video-frame {
       position: relative;
       padding-top: 56.25%; /* 16:9 */
       background-color: #000;
       border-radius: 8px;
       overflow: hidden;
   }
   .video-frame iframe {
       position: absolute;
       top: 0;
       left: 0;
       width: 100%;
       height: 100%;
       border: 0;
   }
   .video-grid {
       display: grid;
       grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
       gap: 20px;
       margin-top: 20px;
   }
   .video-card {
       background: #fff;
       border-radius: 8px;
       box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
       overflow: hidden;
       display: flex;
       flex-direction: column;
   }
   .video-card img {
       width: 100%;
       display: block;
   }
   .video-card .info {
       padding: 10px 12px;
       flex: 1;
   }
   .video-card .title {
       font-weight: bold;
       color: #333;
       margin-bottom: 5px;
   }
   .video-card .meta {
       font-size: 0.85rem;
       color: #666;
   }
   .video-card .actions {
       display: flex;
       gap: 8px;
       padding: 0 12px 12px;
   }
   .video-card .actions > * {
       flex: 1;
   }
   .video-card .actions .btn {
       width: 100%;
       padding: 8px 10px;
       font-size: 0.9rem;
   }
   .pagination {
       display: flex;
       justify-content: space-between;
       margin-top: 20px;
   }
   .yt-form .form-group {
       margin-bottom: 15px;
   }
   .yt-form label {
       display: block;
       font-weight: bold;
       margin-bottom: 5px;
       color: #333;
   }
   .yt-form input[type="text"],
   .yt-form textarea {
       width: 100%;
   }
   .yt-form textarea {
       resize: vertical;
       min-height: 80px;
   }
   .hint {
       font-size: 0.85rem;
       color: #666;
   }
</style>
<div class="youtube-container">
   <h1>YouTube</h1>

   <h2>Search YouTube</h2>
   <c:choose>
       <c:when test="${apiEnabled}">
           <form action="${pageContext.request.contextPath}/youtube" method="get" class="search-form">
               <input type="text" name="q" value="${fn:escapeXml(query)}" placeholder="e.g. photosynthesis for kids" required>
               <select name="classId" title="Also add chosen videos to this class">
                   <option value="">Add to library only</option>
                   <c:forEach var="classroom" items="${classes}">
                       <option value="${classroom.id}" ${classroom.id == classId ? 'selected' : ''}>Also add to: <c:out value="${classroom.name}" /></option>
                   </c:forEach>
               </select>
               <input type="submit" value="Search" class="btn">
           </form>
       </c:when>
       <c:otherwise>
           <div class="notice">
               YouTube search and playlist import need a YouTube Data API key
               (set the <code>YOUTUBE_API_KEY</code> environment variable, see the README).
               You can still add videos by link below.
           </div>
       </c:otherwise>
   </c:choose>

   <div class="preview" id="preview">
       <div class="video-frame"><iframe id="preview-frame" title="Video preview"
               allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
               referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe></div>
   </div>

   <c:if test="${not empty result.videos}">
       <div class="video-grid">
           <c:forEach var="video" items="${result.videos}">
               <div class="video-card">
                   <img src="${video.thumbnailUrl}" alt="${fn:escapeXml(video.title)}">
                   <div class="info">
                       <div class="title"><c:out value="${video.title}" /></div>
                       <div class="meta">
                           <c:out value="${video.channelTitle}" />
                           <c:if test="${not empty video.publishedAt}">
                               &middot; <fmt:formatDate value="${video.publishedAt}" pattern="yyyy-MM-dd" />
                           </c:if>
                       </div>
                   </div>
                   <div class="actions">
                       <button type="button" class="btn btn-secondary" onclick="showPreview('${video.videoId}')">Preview</button>
                       <form action="${pageContext.request.contextPath}/youtube/add" method="post">
                           <input type="hidden" name="url" value="${video.videoId}">
                           <input type="hidden" name="title" value="${fn:escapeXml(video.title)}">
                           <input type="hidden" name="classId" value="${classId}">
                           <input type="hidden" name="q" value="${fn:escapeXml(query)}">
                           <input type="hidden" name="pageToken" value="${fn:escapeXml(pageToken)}">
                           <input type="submit" value="Add" class="btn">
                       </form>
                   </div>
               </div>
           </c:forEach>
       </div>
       <c:url var="prevUrl" value="/youtube">
           <c:param name="q" value="${query}" />
           <c:param name="pageToken" value="${result.prevPageToken}" />
           <c:param name="classId" value="${classId}" />
       </c:url>
       <c:url var="nextUrl" value="/youtube">
           <c:param name="q" value="${query}" />
           <c:param name="pageToken" value="${result.nextPageToken}" />
           <c:param name="classId" value="${classId}" />
       </c:url>
       <div class="pagination">
           <span><c:if test="${not empty result.prevPageToken}"><a href="${prevUrl}" class="btn btn-secondary">&laquo; Previous</a></c:if></span>
           <span><c:if test="${not empty result.nextPageToken}"><a href="${nextUrl}" class="btn btn-secondary">Next &raquo;</a></c:if></span>
       </div>
   </c:if>
   <c:if test="${apiEnabled and not empty query and empty result.videos and empty error}">
       <p>No videos found for "<c:out value="${query}" />".</p>
   </c:if>

   <h2>Add a Video by Link</h2>
   <form action="${pageContext.request.contextPath}/youtube/add" method="post" class="yt-form">
       <div class="form-group">
           <label for="url">YouTube Link:</label>
           <input type="text" name="url" id="url" value="${fn:escapeXml(url)}"
                  placeholder="https://www.youtube.com/watch?v=..." required>
       </div>
       <div class="form-group">
           <label for="title">Title${apiEnabled ? ' (optional)' : ''}:</label>
           <input type="text" name="title" id="title" value="${fn:escapeXml(title)}" ${apiEnabled ? '' : 'required'}>
           <c:if test="${apiEnabled}">
               <div class="hint">Leave empty to use the title from YouTube.</div>
           </c:if>
       </div>
       <div class="form-group">
           <label for="description">Description (optional):</label>
           <textarea name="description" id="description"><c:out value="${description}" /></textarea>
       </div>
       <div class="form-group">
           <label for="link-class">Add to class (optional):</label>
           <select name="classId" id="link-class">
               <option value="">None</option>
               <c:forEach var="classroom" items="${classes}">
                   <option value="${classroom.id}" ${classroom.id == classId ? 'selected' : ''}><c:out value="${classroom.name}" /></option>
               </c:forEach>
           </select>
       </div>
       <input type="submit" value="Add to Library" class="btn">
   </form>

   <h2>Import a YouTube Playlist</h2>
   <c:choose>
       <c:when test="${apiEnabled}">
           <form action="${pageContext.request.contextPath}/youtube/import" method="post" class="yt-form">
               <div class="form-group">
                   <label for="playlistUrl">Playlist Link:</label>
                   <input type="text" name="playlistUrl" id="playlistUrl" value="${fn:escapeXml(playlistUrl)}"
                          placeholder="https://www.youtube.com/playlist?list=..." required>
                   <div class="hint">All public videos of the playlist (up to 200) are added to the library.</div>
               </div>
               <div class="form-group">
                   <label for="playlist-class">Add to class (optional):</label>
                   <select name="classId" id="playlist-class">
                       <option value="">None</option>
                       <c:forEach var="classroom" items="${classes}">
                           <option value="${classroom.id}" ${classroom.id == classId ? 'selected' : ''}><c:out value="${classroom.name}" /></option>
                       </c:forEach>
                   </select>
               </div>
               <input type="submit" value="Import Playlist" class="btn">
           </form>
       </c:when>
       <c:otherwise>
           <p class="hint">Available once a YouTube API key is configured.</p>
       </c:otherwise>
   </c:choose>
</div>
<script>
    // Plays a search result on this page before adding it to the library
    function showPreview(videoId) {
        document.getElementById('preview-frame').src =
            'https://www.youtube-nocookie.com/embed/' + videoId + '?autoplay=1';
        var preview = document.getElementById('preview');
        preview.style.display = 'block';
        preview.scrollIntoView({behavior: 'smooth'});
    }
</script>
<%@ include file="footer.jsp" %>

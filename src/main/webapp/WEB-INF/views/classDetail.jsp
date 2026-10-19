<%-- classDetail.jsp --%>
<%@ include file="header.jsp" %>
<style>
   /* Inline CSS for classDetail.jsp */
   .content-container {
       max-width: 1200px;
       margin: 20px auto;
       padding: 20px;
       background-color: #f9f9f9;
       border-radius: 8px;
       box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
       font-family: Arial, sans-serif;
   }
   .content-container h1 {
       text-align: center;
       color: #003366;
       margin-bottom: 5px;
   }
   .content-container h2 {
       color: #003366;
       margin: 30px 0 15px;
   }
   .subtitle {
       text-align: center;
       color: #666;
       margin-bottom: 15px;
   }
   .class-description {
       color: #333;
       white-space: pre-line;
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
   .content-list {
       list-style: none;
       padding: 0;
   }
   .content-list li {
       display: flex;
       align-items: center;
       gap: 15px;
       background: #fff;
       padding: 10px 15px;
       margin-bottom: 10px;
       border-radius: 5px;
       box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
   }
   .content-list .thumb {
       width: 120px;
       height: 68px;
       object-fit: cover;
       border-radius: 4px;
       background-color: #e3e8ee;
       display: flex;
       align-items: center;
       justify-content: center;
       color: #003366;
       font-size: 0.85rem;
       flex-shrink: 0;
   }
   .content-list .details {
       flex: 1;
   }
   .content-list .details a {
       color: #0055a5;
       font-weight: bold;
       text-decoration: none;
   }
   .content-list .meta {
       font-size: 0.85rem;
       color: #666;
   }
   .add-form {
       display: flex;
       flex-wrap: wrap;
       gap: 10px;
   }
   .add-form select {
       flex: 1;
       min-width: 200px;
       padding: 8px;
       border: 1px solid #ccc;
       border-radius: 5px;
   }
   .btn {
       display: inline-block;
       padding: 10px 20px;
       font-size: 1rem;
       color: white;
       background-color: #003366;
       border: none;
       border-radius: 5px;
       cursor: pointer;
       text-decoration: none;
       transition: background-color 0.3s;
   }
   .btn:hover {
       background-color: #0055a5;
   }
   .btn-youtube {
       background-color: #cc0000;
   }
   .btn-youtube:hover {
       background-color: #e62117;
   }
   .danger-zone {
       margin-top: 30px;
       text-align: right;
   }
</style>
<div class="content-container">
   <h1><c:out value="${classroom.name}" /></h1>
   <p class="subtitle">
       <c:if test="${not empty classroom.schoolName}"><c:out value="${classroom.schoolName}" /> &middot; </c:if>
       Teacher: <c:out value="${classroom.teacherName}" />
   </p>
   <c:if test="${not empty classroom.description}">
       <p class="class-description"><c:out value="${classroom.description}" /></p>
   </c:if>

   <c:if test="${not empty playAllUrl}">
       <h2>Play All YouTube Videos</h2>
       <div class="video-frame">
           <iframe src="${playAllUrl}" title="Class playlist"
                   allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                   referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>
       </div>
   </c:if>

   <h2>Class Content (${fn:length(contents)})</h2>
   <ul class="content-list">
       <c:forEach var="item" items="${contents}">
           <li>
               <c:choose>
                   <c:when test="${item.youtube}"><img src="${item.youtubeThumbnailUrl}" alt="" class="thumb"></c:when>
                   <c:otherwise><span class="thumb">${item.typeLabel}</span></c:otherwise>
               </c:choose>
               <div class="details">
                   <a href="${pageContext.request.contextPath}/content/view/${item.id}"><c:out value="${item.title}" /></a>
                   <div class="meta">
                       ${item.typeLabel}
                       <c:if test="${item.youtube and not empty item.youtubeVideo.durationLabel}"> &middot; ${item.youtubeVideo.durationLabel}</c:if>
                       &middot; added by <c:out value="${item.uploaderName}" />
                   </div>
               </div>
               <form action="${pageContext.request.contextPath}/classes/${classroom.id}/content/${item.id}/remove" method="post" class="inline">
                   <input type="submit" value="Remove" class="btn-small btn-danger">
               </form>
           </li>
       </c:forEach>
       <c:if test="${empty contents}">
           <li>This class has no content yet.</li>
       </c:if>
   </ul>

   <h2>Add Content</h2>
   <c:if test="${not empty availableContents}">
       <form action="${pageContext.request.contextPath}/classes/${classroom.id}/content" method="post" class="add-form">
           <select name="contentId" required>
               <c:forEach var="item" items="${availableContents}">
                   <option value="${item.id}">[${item.typeLabel}] <c:out value="${item.title}" /></option>
               </c:forEach>
           </select>
           <input type="submit" value="Add from Library" class="btn">
       </form>
       <p>or</p>
   </c:if>
   <a href="${pageContext.request.contextPath}/youtube?classId=${classroom.id}" class="btn btn-youtube">Find Videos on YouTube</a>
   <a href="${pageContext.request.contextPath}/content/upload" class="btn">Upload a File</a>

   <div class="danger-zone">
       <form action="${pageContext.request.contextPath}/classes/${classroom.id}/delete" method="post" class="inline"
             onsubmit="return confirm('Delete this class? Its content stays in the library.');">
           <input type="submit" value="Delete Class" class="btn-small btn-danger">
       </form>
   </div>
</div>
<%@ include file="footer.jsp" %>

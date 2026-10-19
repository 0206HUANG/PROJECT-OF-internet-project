<%-- adminDashboard.jsp --%>
<%@ include file="header.jsp" %>
<style>
   /* Inline CSS for adminDashboard.jsp */
   .dashboard-container {
       max-width: 1200px;
       margin: 20px auto;
       padding: 20px;
       background-color: #f9f9f9;
       border-radius: 8px;
       box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
       font-family: Arial, sans-serif;
   }
   .dashboard-container h1 {
       text-align: center;
       color: #003366;
       margin-bottom: 20px;
   }
   .stats-cards {
       display: flex;
       gap: 20px;
       justify-content: space-around;
       margin-bottom: 30px;
   }
   .card {
       flex: 1;
       background-color: #fff;
       padding: 20px;
       border-radius: 8px;
       box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
       text-align: center;
   }
   .card h3 {
       margin-bottom: 10px;
       color: #003366;
   }
   .card p {
       font-size: 1.5rem;
       font-weight: bold;
       color: #333;
   }
   .youtube-status {
       text-align: center;
       color: #555;
   }
   .youtube-status .ok {
       color: #1e6b34;
   }
   .youtube-status .off {
       color: #a12a1f;
   }
   .recent-activities {
       margin-top: 30px;
   }
   .recent-activities h2 {
       color: #003366;
       margin-bottom: 15px;
   }
   .recent-activities ul {
       list-style: none;
       padding: 0;
   }
   .recent-activities ul li {
       background: #fff;
       padding: 10px;
       margin-bottom: 10px;
       border-radius: 5px;
       box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
       font-size: 1rem;
       color: #333;
   }
</style>
<div class="dashboard-container">
   <h1>Administrator Dashboard</h1>
   <div class="stats-cards">
       <div class="card">
           <h3>Total Schools</h3>
           <p>${totalSchools}</p>
       </div>
       <div class="card">
           <h3>Registered Users</h3>
           <p>${totalUsers}</p>
       </div>
       <div class="card">
           <h3>Content Items</h3>
           <p>${totalContents}</p>
       </div>
       <div class="card">
           <h3>Classes</h3>
           <p>${totalClasses}</p>
       </div>
   </div>
   <p class="youtube-status">
       YouTube API:
       <c:choose>
           <c:when test="${youtubeEnabled}"><strong class="ok">connected</strong> (search and playlist import are available)</c:when>
           <c:otherwise><strong class="off">not configured</strong> (videos can still be added by link; set <code>YOUTUBE_API_KEY</code> to enable search)</c:otherwise>
       </c:choose>
   </p>
   <div class="recent-activities">
       <h2>Recent Activities</h2>
       <ul>
           <c:forEach var="activity" items="${activities}">
               <li>
                   <fmt:formatDate value="${activity.time}" pattern="yyyy-MM-dd HH:mm" /> &mdash;
                   <c:out value="${activity.message}" />
               </li>
           </c:forEach>
           <c:if test="${empty activities}">
               <li>No recent activities to show.</li>
           </c:if>
       </ul>
   </div>
</div>
<%@ include file="footer.jsp" %>

<%-- reports.jsp --%>
<%@ include file="header.jsp" %>
<style>
   /* Inline CSS for reports.jsp */
   .report-container {
       max-width: 1200px;
       margin: 20px auto;
       padding: 20px;
       background-color: #f9f9f9;
       border-radius: 8px;
       box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
       font-family: Arial, sans-serif;
   }
   .report-container h1 {
       text-align: center;
       color: #003366;
       margin-bottom: 20px;
   }
   .report-container h2 {
       color: #003366;
       margin: 25px 0 10px;
   }
   table {
       width: 100%;
       border-collapse: collapse;
       background-color: #fff;
       box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
       border-radius: 8px;
       overflow: hidden;
   }
   table td {
       padding: 12px 15px;
       border-bottom: 1px solid #ddd;
       font-size: 0.95rem;
       color: #333;
   }
   table td.value {
       text-align: right;
       font-weight: bold;
       width: 150px;
   }
</style>
<div class="report-container">
   <h1>Reports</h1>

   <h2>Schools</h2>
   <table>
       <tr><td>Total schools</td><td class="value">${totalSchools}</td></tr>
       <tr><td>Active schools</td><td class="value">${activeSchools}</td></tr>
       <tr><td>Inactive schools</td><td class="value">${inactiveSchools}</td></tr>
   </table>

   <h2>Users</h2>
   <table>
       <tr><td>Registered users</td><td class="value">${totalUsers}</td></tr>
       <tr><td>Administrators</td><td class="value">${adminCount}</td></tr>
       <tr><td>Teachers</td><td class="value">${teacherCount}</td></tr>
   </table>

   <h2>Content</h2>
   <table>
       <tr><td>Content items</td><td class="value">${totalContents}</td></tr>
       <tr><td>Uploaded files</td><td class="value">${totalContents - youtubeContents}</td></tr>
       <tr><td>YouTube videos</td><td class="value">${youtubeContents}</td></tr>
   </table>

   <h2>Classes</h2>
   <table>
       <tr><td>Classes</td><td class="value">${totalClasses}</td></tr>
   </table>
</div>
<%@ include file="footer.jsp" %>

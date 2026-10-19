<%-- editContent.jsp --%>
<%@ include file="header.jsp" %>
<style>
    /* Inline CSS for editContent.jsp */
    .form-container {
        max-width: 600px;
        margin: 30px auto;
        padding: 20px;
        background-color: #f9f9f9;
        border-radius: 8px;
        box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
        font-family: Arial, sans-serif;
    }

    .form-container h1 {
        text-align: center;
        color: #003366;
        margin-bottom: 20px;
    }

    .form-group {
        margin-bottom: 15px;
    }

    .form-group label {
        display: block;
        font-weight: bold;
        margin-bottom: 5px;
        color: #333;
    }

    .form-group input[type="text"],
    .form-group textarea {
        width: 100%;
        padding: 10px;
        font-size: 1rem;
        border: 1px solid #ccc;
        border-radius: 5px;
        box-sizing: border-box;
    }

    .form-group textarea {
        resize: vertical;
        min-height: 120px;
    }

    .form-group input[type="submit"] {
        width: 100%;
        padding: 10px 20px;
        font-size: 1rem;
        color: white;
        background-color: #003366;
        border: none;
        border-radius: 5px;
        cursor: pointer;
        transition: background-color 0.3s;
    }

    .form-group input[type="submit"]:hover {
        background-color: #0055a5;
    }

    .cancel {
        text-align: center;
    }

    .cancel a {
        color: #0055a5;
    }
</style>
<div class="form-container">
    <h1>Edit Content</h1>
    <form action="${pageContext.request.contextPath}/content/edit/${content.id}" method="post">
        <div class="form-group">
            <label for="title">Title:</label>
            <input type="text" name="title" id="title" value="${fn:escapeXml(title)}" required>
        </div>
        <div class="form-group">
            <label for="description">Description${content.youtube ? ' (optional)' : ''}:</label>
            <textarea name="description" id="description" ${content.youtube ? '' : 'required'}><c:out value="${description}" /></textarea>
        </div>
        <div class="form-group">
            <input type="submit" value="Save Changes">
        </div>
    </form>
    <p class="cancel"><a href="${pageContext.request.contextPath}/content/view/${content.id}">Cancel</a></p>
</div>
<%@ include file="footer.jsp" %>

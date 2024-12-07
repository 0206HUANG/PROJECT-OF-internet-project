<!-- uploadContent.jsp -->
<%@ include file="header.jsp" %>
<style>
    /* Inline CSS for uploadContent.jsp */
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
    .form-group input[type="file"],
    .form-group textarea {
        width: 100%;
        padding: 10px;
        font-size: 1rem;
        border: 1px solid #ccc;
        border-radius: 5px;
        box-sizing: border-box;
        outline: none;
        transition: border-color 0.3s;
    }

    .form-group input[type="text"]:focus,
    .form-group textarea:focus,
    .form-group input[type="file"]:focus {
        border-color: #003366;
    }

    .form-group textarea {
        resize: vertical;
        min-height: 100px;
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
</style>
<div class="form-container">
    <h1>Upload New Content</h1>
    <form action="${pageContext.request.contextPath}/content/save" method="post" enctype="multipart/form-data">
        <div class="form-group">
            <label for="title">Content Title:</label>
            <input type="text" name="title" id="title" required>
        </div>
        <div class="form-group">
            <label for="description">Description:</label>
            <textarea name="description" id="description" required></textarea>
        </div>
        <div class="form-group">
            <label for="file">Select File:</label>
            <input type="file" name="file" id="file" accept="video/*,image/*" required>
        </div>
        <div class="form-group">
            <input type="submit" value="Upload Content">
        </div>
    </form>
</div>
<%@ include file="footer.jsp" %>

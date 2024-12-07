<!-- addSchool.jsp -->
<%@ include file="header.jsp" %>
<style>
/* Inline CSS styles for addSchool.jsp */
.form-container {
    max-width: 600px;
    margin: 50px auto;
    padding: 30px;
    background-color: #f9f9f9;
    border-radius: 8px;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
    font-family: Arial, sans-serif;
}

.form-title {
    font-size: 1.8rem;
    text-align: center;
    margin-bottom: 20px;
    color: #003366;
}

.styled-form {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.form-group {
    display: flex;
    flex-direction: column;
}

.form-label {
    font-weight: bold;
    margin-bottom: 8px;
    color: #333;
}

.form-input,
.form-select {
    padding: 10px;
    font-size: 1rem;
    border: 1px solid #ccc;
    border-radius: 5px;
    outline: none;
    transition: border-color 0.3s;
}

.form-input:focus,
.form-select:focus {
    border-color: #003366;
}

.form-submit-btn {
    padding: 12px 20px;
    font-size: 1rem;
    color: #fff;
    background-color: #003366;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    transition: background-color 0.3s;
}

.form-submit-btn:hover {
    background-color: #0055a5;
}
</style>
<div class="form-container">
    <h1 class="form-title">Add New School</h1>
    <form action="${pageContext.request.contextPath}/schools/save" method="post" class="styled-form">
        <div class="form-group">
            <label for="name" class="form-label">School Name:</label>
            <input type="text" name="name" id="name" class="form-input" required>
        </div>
        <div class="form-group">
            <label for="location" class="form-label">Location:</label>
            <input type="text" name="location" id="location" class="form-input" required>
        </div>
        <div class="form-group">
            <label for="status" class="form-label">Status:</label>
            <select name="status" id="status" class="form-select">
                <option value="Active">Active</option>
                <option value="Inactive">Inactive</option>
            </select>
        </div>
        <div class="form-group">
            <input type="submit" value="Save School" class="form-submit-btn">
        </div>
    </form>
</div>
<%@ include file="footer.jsp" %>



import React, { useState } from 'react';
import axios from 'axios';

function FileUpload() {
  const [file, setFile] = useState(null);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadMessage, setUploadMessage] = useState('');

  const handleFileChange = (event) => {
    setFile(event.target.files[0]);
    setUploadMessage('');
  };

  const handleUpload = async () => {
    if (!file) {
      alert("Please select a file first.");
      return;
    }

    setIsUploading(true);
    setUploadMessage('');

    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await axios.post("http://localhost:8080/app/upload", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
        timeout: 60000, // 60 seconds timeout
      });

      setUploadMessage(response.data || "Upload successful!");
    } catch (error) {
      if (error.code === 'ECONNABORTED') {
        setUploadMessage("Upload timed out. Please try again.");
      } else {
        setUploadMessage("Upload failed. Please try again.");
      }
      console.error("Upload error:", error);
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <div style={{ padding: "1rem", maxWidth: "300px" }}>
      <h3>Upload File</h3>
      <input type="file" onChange={handleFileChange} disabled={isUploading} />
      {file && <p>Selected: {file.name}</p>}
      <button
        onClick={handleUpload}
        disabled={isUploading}
        style={{
          marginTop: "0.5rem",
          padding: "0.5rem 1rem",
          backgroundColor: isUploading ? "#ccc" : "#007bff",
          color: "white",
          border: "none",
          borderRadius: "4px",
          cursor: isUploading ? "not-allowed" : "pointer",
        }}
      >
        {isUploading ? "Uploading..." : "Upload"}
      </button>
      {isUploading && <div style={{ marginTop: "0.5rem" }}>🔄 Please wait...</div>}
      {uploadMessage && <div style={{ marginTop: "0.5rem" }}>{uploadMessage}</div>}
    </div>
  );
}

export default FileUpload;

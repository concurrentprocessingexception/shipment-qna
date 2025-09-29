import './App.css';
import Chat from './Chat';
import Header from './Header';
import Footer from './Footer';
import FileUpload from './FileUpload';

function App() {
  return (
    <div className="App">
      <Header />
      <div className="app-container">
        <div className="static-container">
          <FileUpload />
        </div>
        <div className="static-container">
          
        </div>
        <div className="chat-container">
          <Chat />
        </div>
      </div>
      <Footer />
    </div>
  );
}

export default App;

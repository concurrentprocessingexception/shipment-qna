import React, { useState, useRef, useEffect } from 'react';
import axios from 'axios';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faWindowMinimize } from '@fortawesome/free-regular-svg-icons';
import './Chat.css';

const Chat = () => {
    const [inputValue, setInputValue] = useState('');
    const [conversations, setConversations] = useState([{ message: "Hi!!! How can I assist you today?", from: 'AI' }]);
    const [isChatOpen, setIsChatOpen] = useState(false);
    const conversationContainerRef = useRef(null);

    const sendMessage = async (e) => {
        e.preventDefault();
        if (inputValue.trim() !== '') {
            try {
                const response = await axios.post('http://localhost:8080/app/chat', {
                    message: inputValue,
                    history: conversations,
                });
                const aiResponseMessage = response.data.message;
                const aiResponse = { message: aiResponseMessage, from: 'AI' };
                setConversations([...conversations, { message: inputValue, from: 'You' }, aiResponse]); // Add both user's message and AI's response to the conversation
                setInputValue('');
            } catch (error) {
                console.error('Error sending message:', error);
            }
        }
    };

    useEffect(() => {
        if (conversationContainerRef.current) {
          conversationContainerRef.current.scrollTop = conversationContainerRef.current.scrollHeight;
        }
      }, [conversations]);

    const toggleChat = () => {
        setIsChatOpen(!isChatOpen);
    };

    const minimizeChat = () => {
        setIsChatOpen(false);
    };

    return (
        <div className="chat-box">
            {!isChatOpen && (
                <div className="toggle-chat" onClick={toggleChat}>
                    <i className="fas fa-comment"></i> {/* Add your chat icon */}
                </div>
            )}
            {isChatOpen && (
                <div className="chat-container">
                    <div className="minimize-chat" onClick={minimizeChat}>
                      <FontAwesomeIcon icon={faWindowMinimize} />
                    </div>
                    <div ref={conversationContainerRef} className="conversation-container">
                        {conversations.map((conversation, index) => (
                            <p key={index} className={conversation.from === 'AI' ? 'ai-message' : 'user-message'}>
                                {conversation.from}: {conversation.message}
                            </p>
                        ))}
                    </div>
                    <form onSubmit={sendMessage} className="message-form">
                        <input
                            type="text"
                            value={inputValue}
                            onChange={(e) => setInputValue(e.target.value)}
                            placeholder="Type your message..."
                            className="message-input"
                        />
                        <button type="submit" className="send-button">Send</button>
                    </form>
                </div>
            )}
        </div>
    );
};

export default Chat;

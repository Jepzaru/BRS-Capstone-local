import React, { useState } from 'react';
import '../CSS/UserCss/Login.css';
import { Link } from 'react-router-dom';
import { FaUser } from 'react-icons/fa';
import logoImage from "../Images/citlogo1.png";
import logoImage1 from "../Images/citbglogo.png";

const ForgotPassword = () => {
    const [email, setEmail] = useState('');

    const handleClear = () => {
        setEmail('');
      };

  return (
    <div className="login-page">
      <img src={logoImage} alt="Logo" className="logo-image" />
      <div className="label-container">
        <h1 className="label-text">TRANSPORTATION RESERVATION SYSTEM</h1>
      </div>
      <div className="login-container">
        <form className="login-form">
          <h2>Forgot Password</h2>
          <div className="input-group">
            <span className="icon"><FaUser /></span>
            <input
              type="email"
              id="email"
              className="input-field"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder='Email'
              required
            />
          </div>
          <button type="submit" className="login-button">EMAIL ME!</button>
          <button type="button" className="clear-button" onClick={handleClear}>CLEAR ENTITIES</button>
          <p className='admin-path'>
            Back to Login page <Link to="/user-authentication">Click Here</Link>
          </p>
        </form>
      </div>
      <img src={logoImage1} alt="Logo" className="logo-image1" />
    </div>
  );
};

export default ForgotPassword;

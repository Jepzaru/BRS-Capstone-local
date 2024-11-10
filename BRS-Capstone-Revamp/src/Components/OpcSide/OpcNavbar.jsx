import React, { useState, useEffect } from 'react';
import { NavLink } from 'react-router-dom';
import '../../CSS/UserCss/SideNavbar.css';
import ToggleButton from '../../Components/UserSide/ToggleButton';
import { MdDashboard } from "react-icons/md";
import { FaFileLines } from "react-icons/fa6";
import { FaBus } from "react-icons/fa";
import { GiCarSeat } from "react-icons/gi";
import { FaGear } from "react-icons/fa6";
import { FaClipboardCheck } from "react-icons/fa6";
import { FaCalendarDay } from "react-icons/fa";

const OpcNavbar = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [requestCount, setRequestCount] = useState(0);

  const handleToggle = () => {
    setIsOpen(!isOpen);
  };
  
  useEffect(() => {
    const savedCount = localStorage.getItem('requestCount');
    if (savedCount) {
      setRequestCount(parseInt(savedCount, 10));
    }
  }, []);

  useEffect(() => {
    if (requestCount > 0) {
      localStorage.setItem('requestCount', requestCount);
    }
  }, [requestCount]);

  return (
    <>
      <ToggleButton onClick={handleToggle} />
      <nav className={`side-navbar ${isOpen ? 'open' : ''}`}>
        <ul>
          <li>
            <NavLink to="/dashboard" activeClassName="active-link">
              <MdDashboard style={{ marginRight: "15px", marginBottom: "-2px" }} />Dashboard
            </NavLink>
          </li>
          <li>
            <NavLink to="/opc-requests" activeClassName="active-link">
              <FaFileLines style={{ marginRight: "15px", marginBottom: "-2px" }} />
              Requests
              {requestCount > 0 && <span className="notification-badge">{requestCount}</span>}
            </NavLink>
          </li>
          <li>
            <NavLink to="/opc-approved-requests" activeClassName="active-link">
              <FaClipboardCheck style={{ marginRight: "15px", marginBottom: "-2px" }} />Approved Requests
            </NavLink>
          </li>
          <li>
            <NavLink to="/opc-bigcalendar" activeClassName="active-link">
              <FaCalendarDay style={{ marginRight: "15px", marginBottom: "-2px" }} />Calendar Events
            </NavLink>
          </li>
          <li>
            <NavLink to="/vehicle-management" activeClassName="active-link">
              <FaBus style={{ marginRight: "15px", marginBottom: "-2px" }} />Vehicle Management
            </NavLink>
          </li>
          <li>
            <NavLink to="/driver-management" activeClassName="active-link">
              <GiCarSeat style={{ marginRight: "15px", marginBottom: "-2px" }} />Driver Management
            </NavLink>
          </li>
          <li>
            <NavLink to="/opc-settings" activeClassName="active-link">
              <FaGear style={{ marginRight: "15px", marginBottom: "-2px" }} />Settings
            </NavLink>
          </li>
        </ul>
      </nav>
    </>
  );
};

export default OpcNavbar;

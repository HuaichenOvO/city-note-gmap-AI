import React from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/authContext";

export const NavBar = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
    };

    return (
        <nav className="bg-white shadow-sm border-b border-gray-200 px-4 py-2">
            <div className="flex justify-between items-center">
                <div className="flex items-center space-x-4">
                    <h1 className="text-xl font-semibold text-gray-800">City Note</h1>
                </div>
                
                <div className="flex items-center space-x-4">
                    {user && (
                        <>
                            <div 
                                className="flex items-center space-x-2 cursor-pointer hover:bg-gray-100 p-2 rounded-md transition-colors duration-200"
                                onClick={() => navigate('/profile')}
                            >
                                <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center">
                                    <span className="text-white text-sm font-medium">
                                        {user.username.charAt(0).toUpperCase()}
                                    </span>
                                </div>
                                <span className="text-gray-700 font-medium">{user.username}</span>
                            </div>
                            <button
                                onClick={handleLogout}
                                className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-md transition-colors duration-200"
                            >
                                Logout
                            </button>
                        </>
                    )}
                </div>
            </div>
        </nav>
    );
};
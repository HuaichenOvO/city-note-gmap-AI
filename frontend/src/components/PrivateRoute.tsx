import React, { useEffect, useState } from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/authContext';

const PrivateRoute: React.FC = () => {
  const { isAuthenticated, loading } = useAuth();
  const [isChecking, setIsChecking] = useState(true);

  useEffect(() => {
    // 如果认证状态还在加载中，等待加载完成
    if (!loading) {
      setIsChecking(false);
    }
  }, [loading]);

  // 如果还在检查认证状态，显示加载状态
  if (loading || isChecking) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  // 如果未认证，重定向到登录页面
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // 如果已认证，渲染子组件
  return <Outlet />;
};

export default PrivateRoute; 
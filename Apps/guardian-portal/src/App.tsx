import React, { useState } from 'react';
import { Route, Switch } from 'wouter';
import { WardProvider } from './contexts/WardContext';
import { Layout } from './components/Layout';
import { Dashboard } from './pages/Dashboard';
import { ReportCardsPage } from './pages/ReportCards';
import { AttendancePage } from './pages/Attendance';
import { FinancePage } from './pages/Finance';
import { AnnouncementsPage } from './pages/Announcements';
import { LoginPage } from './pages/Login';

export function App() {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(() => {
    return !!localStorage.getItem('ubs_guardian_token');
  });

  if (!isAuthenticated) {
    return <LoginPage onLoginSuccess={() => setIsAuthenticated(true)} />;
  }

  return (
    <WardProvider>
      <Layout>
        <Switch>
          <Route path="/" component={Dashboard} />
          <Route path="/report-cards" component={ReportCardsPage} />
          <Route path="/attendance" component={AttendancePage} />
          <Route path="/finance" component={FinancePage} />
          <Route path="/announcements" component={AnnouncementsPage} />
          <Route>
            <Dashboard />
          </Route>
        </Switch>
      </Layout>
    </WardProvider>
  );
}

export default App;

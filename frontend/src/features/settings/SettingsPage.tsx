import { useState } from 'react';
import { User, Building, Lock, Bell, Palette } from 'lucide-react';
import { Button, Input, Card, CardHeader, CardTitle, CardContent } from '@/components/ui';
import { useAuthStore } from '@/stores/auth.store';
import toast from 'react-hot-toast';

export function SettingsPage() {
    const { user } = useAuthStore();
    const [activeTab, setActiveTab] = useState('profile');
    const [isLoading, setIsLoading] = useState(false);

    const tabs = [
        { id: 'profile', label: 'Profile', icon: User },
        { id: 'company', label: 'Company', icon: Building },
        { id: 'security', label: 'Security', icon: Lock },
        { id: 'notifications', label: 'Notifications', icon: Bell },
        { id: 'appearance', label: 'Appearance', icon: Palette },
    ];

    const handleSave = async () => {
        setIsLoading(true);
        // Simulate save
        await new Promise((resolve) => setTimeout(resolve, 1000));
        toast.success('Settings saved');
        setIsLoading(false);
    };

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-3xl font-bold text-foreground">Settings</h1>
                <p className="text-muted-foreground">Manage your account and preferences</p>
            </div>

            <div className="flex flex-col gap-6 lg:flex-row">
                {/* Sidebar */}
                <nav className="w-full lg:w-48">
                    <ul className="space-y-1">
                        {tabs.map((tab) => (
                            <li key={tab.id}>
                                <button
                                    onClick={() => setActiveTab(tab.id)}
                                    className={`flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${activeTab === tab.id
                                        ? 'bg-primary text-primary-foreground'
                                        : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
                                        }`}
                                >
                                    <tab.icon className="h-4 w-4" />
                                    {tab.label}
                                </button>
                            </li>
                        ))}
                    </ul>
                </nav>

                {/* Content */}
                <div className="flex-1">
                    {activeTab === 'profile' && (
                        <Card>
                            <CardHeader>
                                <CardTitle>Profile Settings</CardTitle>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <div className="flex items-center gap-4">
                                    <div className="flex h-20 w-20 items-center justify-center rounded-full bg-primary text-2xl font-bold text-primary-foreground">
                                        {user?.firstName?.charAt(0) || 'U'}
                                        {user?.lastName?.charAt(0) || ''}
                                    </div>
                                    <div>
                                        <Button variant="outline" size="sm">
                                            Change Avatar
                                        </Button>
                                    </div>
                                </div>
                                <div className="grid gap-4 sm:grid-cols-2">
                                    <Input label="First Name" defaultValue={user?.firstName || ''} />
                                    <Input label="Last Name" defaultValue={user?.lastName || ''} />
                                </div>
                                <Input label="Email" type="email" defaultValue={user?.email || ''} />
                                <div className="flex justify-end">
                                    <Button onClick={handleSave} isLoading={isLoading}>
                                        Save Changes
                                    </Button>
                                </div>
                            </CardContent>
                        </Card>
                    )}

                    {activeTab === 'company' && (
                        <Card>
                            <CardHeader>
                                <CardTitle>Company Settings</CardTitle>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <Input label="Company Name" defaultValue="My Company" />
                                <Input label="Tax ID / GST Number" defaultValue="GSTIN1234567890" />
                                <Input label="Address" defaultValue="123 Business Street" />
                                <div className="grid gap-4 sm:grid-cols-3">
                                    <Input label="City" defaultValue="Mumbai" />
                                    <Input label="State" defaultValue="Maharashtra" />
                                    <Input label="Postal Code" defaultValue="400001" />
                                </div>
                                <Input label="Phone" defaultValue="+91 9876543210" />
                                <Input label="Email" defaultValue="company@example.com" />
                                <div className="flex justify-end">
                                    <Button onClick={handleSave} isLoading={isLoading}>
                                        Save Changes
                                    </Button>
                                </div>
                            </CardContent>
                        </Card>
                    )}

                    {activeTab === 'security' && (
                        <Card>
                            <CardHeader>
                                <CardTitle>Security Settings</CardTitle>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <div className="rounded-lg border border-border p-4">
                                    <h3 className="font-medium text-foreground">Change Password</h3>
                                    <p className="mb-4 text-sm text-muted-foreground">
                                        Update your password to keep your account secure
                                    </p>
                                    <div className="space-y-3">
                                        <Input label="Current Password" type="password" />
                                        <Input label="New Password" type="password" />
                                        <Input label="Confirm New Password" type="password" />
                                    </div>
                                    <div className="mt-4">
                                        <Button onClick={handleSave} isLoading={isLoading}>
                                            Update Password
                                        </Button>
                                    </div>
                                </div>

                                <div className="rounded-lg border border-border p-4">
                                    <h3 className="font-medium text-foreground">Two-Factor Authentication</h3>
                                    <p className="mb-4 text-sm text-muted-foreground">
                                        Add an extra layer of security to your account
                                    </p>
                                    <Button variant="outline">Enable 2FA</Button>
                                </div>
                            </CardContent>
                        </Card>
                    )}

                    {activeTab === 'notifications' && (
                        <Card>
                            <CardHeader>
                                <CardTitle>Notification Preferences</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <div className="space-y-4">
                                    {[
                                        { label: 'Low stock alerts', description: 'Get notified when products are running low' },
                                        { label: 'New orders', description: 'Receive notifications for new orders' },
                                        { label: 'Payment received', description: 'Get notified when payments are received' },
                                        { label: 'Weekly reports', description: 'Receive weekly summary reports' },
                                    ].map((item, index) => (
                                        <div key={index} className="flex items-center justify-between rounded-lg border border-border p-4">
                                            <div>
                                                <p className="font-medium text-foreground">{item.label}</p>
                                                <p className="text-sm text-muted-foreground">{item.description}</p>
                                            </div>
                                            <label className="relative inline-flex cursor-pointer items-center">
                                                <input type="checkbox" defaultChecked className="peer sr-only" />
                                                <div className="peer h-6 w-11 rounded-full bg-muted after:absolute after:left-[2px] after:top-[2px] after:h-5 after:w-5 after:rounded-full after:bg-white after:transition-all peer-checked:bg-primary peer-checked:after:translate-x-full"></div>
                                            </label>
                                        </div>
                                    ))}
                                </div>
                            </CardContent>
                        </Card>
                    )}

                    {activeTab === 'appearance' && (
                        <Card>
                            <CardHeader>
                                <CardTitle>Appearance</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <div className="space-y-4">
                                    <div>
                                        <p className="mb-3 font-medium text-foreground">Theme</p>
                                        <div className="grid grid-cols-3 gap-3">
                                            {['Dark', 'Light', 'System'].map((theme) => (
                                                <button
                                                    key={theme}
                                                    className={`rounded-lg border p-4 text-center transition-colors ${theme === 'Dark'
                                                        ? 'border-primary bg-primary/10'
                                                        : 'border-border hover:bg-accent'
                                                        }`}
                                                >
                                                    <div
                                                        className={`mx-auto mb-2 h-8 w-8 rounded-lg ${theme === 'Dark' ? 'bg-slate-800' : theme === 'Light' ? 'bg-white border' : 'bg-gradient-to-r from-slate-800 to-white'
                                                            }`}
                                                    />
                                                    <span className="text-sm font-medium">{theme}</span>
                                                </button>
                                            ))}
                                        </div>
                                    </div>

                                    <div>
                                        <p className="mb-3 font-medium text-foreground">Language</p>
                                        <select className="h-10 w-full rounded-lg border border-input bg-background px-3 text-sm focus:outline-none focus:ring-2 focus:ring-ring">
                                            <option value="en">English</option>
                                            <option value="hi">Hindi</option>
                                            <option value="kn">Kannada</option>
                                            <option value="ta">Tamil</option>
                                        </select>
                                    </div>

                                    <div>
                                        <p className="mb-3 font-medium text-foreground">Currency</p>
                                        <select className="h-10 w-full rounded-lg border border-input bg-background px-3 text-sm focus:outline-none focus:ring-2 focus:ring-ring">
                                            <option value="INR">Indian Rupee (₹)</option>
                                            <option value="USD">US Dollar ($)</option>
                                            <option value="EUR">Euro (€)</option>
                                        </select>
                                    </div>
                                </div>
                            </CardContent>
                        </Card>
                    )}
                </div>
            </div>
        </div>
    );
}

export { SettingsPage as default };

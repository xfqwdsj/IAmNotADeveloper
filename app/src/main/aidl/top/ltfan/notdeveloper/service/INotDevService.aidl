package top.ltfan.notdeveloper.service;

import top.ltfan.notdeveloper.service.INotificationCallback;
import top.ltfan.notdeveloper.service.data.IPackageSettingsDao;

interface INotDevService {
    void notifySettingChange(
        String name,
        int type,
        INotificationCallback callback
    );

    Map<String, IPackageSettingsDao> getConnections();
}

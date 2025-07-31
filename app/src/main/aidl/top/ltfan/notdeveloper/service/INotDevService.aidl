package top.ltfan.notdeveloper.service;

import top.ltfan.notdeveloper.service.INotificationCallback;

interface INotDevService {
    void notifySettingChange(
        String name,
        int type,
        INotificationCallback callback
    );
}

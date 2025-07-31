package top.ltfan.notdeveloper;

import top.ltfan.notdeveloper.INotificationCallback;
import top.ltfan.notdeveloper.detection.DetectionMethod;

interface INotDevService {
    void notifySettingChange(
        in DetectionMethod method,
        INotificationCallback callback
    );
}

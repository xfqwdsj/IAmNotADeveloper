package top.ltfan.notdeveloper.service.data;

import top.ltfan.notdeveloper.data.ParcelablePackageInfo;
import top.ltfan.notdeveloper.service.data.IPackageInfoListListener;
import top.ltfan.notdeveloper.service.data.IBooleanListener;
import top.ltfan.notdeveloper.service.data.IUnlistener;

interface IPackageSettingsDao {
    void insertPackageInfo(String packageName, int userId, int appId);
    void deletePackageInfo(String packageName, int userId);
    List<ParcelablePackageInfo> getPackageInfoByName(String packageName);
    IUnlistener listenPackageInfoByName(String packageName, IPackageInfoListListener listener);
    List<ParcelablePackageInfo> getPackageInfoByUser(int userId);
    IUnlistener listenPackageInfoByUser(int userId, IPackageInfoListListener listener);
    boolean isPackageExists(String packageName, int userId);
    boolean isDetectionSet(String packageName, int userId, String methodName);
    boolean isDetectionEnabled(String packageName, int userId, String methodName);
    IUnlistener listenDetectionEnabled(String packageName, int userId, String methodName, IBooleanListener listener);
    void clearAllData();
    void toggleDetectionEnabled(String packageName, int userId, String methodName);
    void enableAllDetectionsForPackage(String packageName, int userId);
    void disableAllDetectionsForPackage(String packageName, int userId);
}

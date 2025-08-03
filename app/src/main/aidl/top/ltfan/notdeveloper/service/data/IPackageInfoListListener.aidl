package top.ltfan.notdeveloper.service.data;

import top.ltfan.notdeveloper.data.ParcelablePackageInfo;

interface IPackageInfoListListener {
    void invoke(out List<ParcelablePackageInfo> packageInfo);
}

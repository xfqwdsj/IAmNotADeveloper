[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/xfqwdsj/IAmNotADeveloper)

[简体中文](README.md) | [English](README_en.md) | [Português (Brasil)](README_pt-BR.md) | **Türkçe**

# IAmNotADeveloper

Android sistem geliştirici seçeneklerinin durumunu gizlemek için bir modül.

## SSS

### S: Modülü etkinleştirdim, ancak "Modül etkinleştirilmedi" diyor. Ne yapmalıyım?

Sorun giderme adımları:

1. Modülü etkinleştirdiğinizden emin olun.
2. Modül uygulamasını başarılı bir etkinleştirmeden sonra zorla durdurduğunuzdan emin olun.
3. [Issues](https://github.com/xfqwdsj/IAmNotADeveloper/issues) bölümünde ilgili sorunları arayın.
4. İlgili bir sorun bulamazsanız, lütfen günlükleri yakalayın ve modül uygulamasının ekran görüntülerini alın, modülün tam durum kartının görünür olduğundan emin olun (bir ekran görüntüsüne sığmıyorsa, birden fazla kullanın).
5. [Issues](https://github.com/xfqwdsj/IAmNotADeveloper/issues) bölümünde yeni bir sorun gönderin ve günlükleri yükleyin.

### S: Modülün LSPosed'da aktif olduğunu nasıl teyit edebilirim?

Aşağıdaki adımları izleyerek teyit edebilirsiniz:

1. Herhangi bir yöntemle LSPosed Manager'ı başlatın.
2. "Modüller" sayfasında "IAmNotADeveloper" modülünü bulun.
3. "Modülü etkinleştir" düğmesinin açık olduğundan emin olun.

### S: Günlükleri nasıl yakalarım?

Günlükleri aşağıdaki adımları izleyerek yakalayabilirsiniz:

1. LSPosed Manager'ı herhangi bir yöntemle başlatın.
2. "Günlükler" sayfasında, sağ üst köşedeki "Kaydet" simge düğmesine dokunun.
3. "İndirilenler" gibi uygun bir kaydetme konumu seçin ve dosya adını değiştirmeyin.
4. "Kaydet" düğmesine dokunun.

### S: Belirli bir uygulama için modülü etkinleştirdim, ancak uygulama çöküyor/hiçbir etkisi yok/bir dedektör uygulaması tarafından algılanıyor. Ne yapmalıyım?

Bu modül, hedef uygulamaya doğrudan enjekte edilerek çalışır. Entegre enjeksiyon korumasına sahip uygulamalar için modül çalışmayabilir veya uygulamanın çalışmayı reddetmesine neden olabilir.

Çözüm: Şu anda yok. Ayrıntılar için [Issue #104](https://github.com/xfqwdsj/IAmNotADeveloper/issues/104) sayfasına bakın. Lütfen sabırlı olun; şu anda bir ETA (Tahmini Varış Zamanı) yok. **Bu sorunla ilgili herhangi bir sorun göndermeyin; açıklama yapılmadan kapatılacaktır.**

## Katkıda Bulunma

Eğer bu projeye kod katkısında bulunmak istiyorsanız, lütfen [CONTRIBUTING.md](CONTRIBUTING_tr-TR.md) dosyasına bakın.

## Gizlilik Sözleşmesi

Bu uygulamanın "Test" işlevi, aşağıdaki sistem anahtarlarının durumunu alacaktır:

- Geliştirici seçenekleri
- USB hata ayıklama
- Kablosuz hata ayıklama

Ancak, bu uygulama sizinle ilgili herhangi bir bilgi toplamayacaktır.
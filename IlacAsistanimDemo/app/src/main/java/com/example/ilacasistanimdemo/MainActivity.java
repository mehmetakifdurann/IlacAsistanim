package com.example.ilacasistanimdemo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int BLUE = Color.rgb(23, 107, 255);
    private static final int GREEN = Color.rgb(19, 143, 94);
    private static final int RED = Color.rgb(216, 45, 45);
    private static final int BG = Color.rgb(247, 248, 250);
    private static final int CARD = Color.WHITE;
    private static final int TEXT = Color.rgb(29, 34, 43);
    private static final int MUTED = Color.rgb(101, 111, 128);

    private final DemoStore store = new DemoStore();
    private User currentUser;
    private Calendar visibleMonth = Calendar.getInstance(new Locale("tr", "TR"));
    private Calendar selectedDay = Calendar.getInstance(new Locale("tr", "TR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 10);
        }
        currentUser = store.users.get(0);
        showLogin();
    }

    private void showLogin() {
        LinearLayout root = page();
        TextView title = title("İlaç Asistanım");
        TextView subtitle = body("İlaç takibi, sorumlu bilgilendirme ve hasta güvenliği için mobil asistan.");
        root.addView(title);
        root.addView(subtitle);
        root.addView(space(18));

        EditText email = input("E-posta");
        EditText password = input("Şifre");
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        email.setText("hasta.demo@ilac.app");
        password.setText("123456");
        root.addView(email);
        root.addView(password);

        Button login = primary("Devam Et");
        root.addView(login);
        login.setOnClickListener(v -> {
            User found = store.login(email.getText().toString().trim(), password.getText().toString());
            if (found == null) {
                toast("Demo hesap bulunamadı. Aşağıdaki hızlı girişlerden birini seçebilirsiniz.");
            } else {
                currentUser = found;
                showHome();
            }
        });

        root.addView(sectionLabel("Demo hesaplar"));
        for (User user : store.users) {
            Button b = outline(user.fullName + " - " + user.role);
            root.addView(b);
            b.setOnClickListener(v -> {
                currentUser = user;
                showHome();
            });
        }

        setContentView(wrap(root));
    }

    private void showHome() {
        if ("Hasta".equals(currentUser.role)) {
            showPatientHome();
        } else {
            showCaregiverHome();
        }
    }

    private void showPatientHome() {
        LinearLayout root = page();
        root.addView(topBar("Hasta Paneli", currentUser.fullName));
        root.addView(connectionPill(store.patientCode));
        root.addView(treatmentSummaryCard());
        root.addView(calendarCard());

        LinearLayout actions = horizontal();
        Button add = primary("İlaç Ekle");
        Button symptom = outline("Semptom");
        actions.addView(add, weight());
        actions.addView(symptom, weight());
        root.addView(actions);

        Button emergency = danger("ACİL");
        root.addView(emergency);

        root.addView(sectionLabel(selectedDayLabel() + " ilaçları"));
        List<DoseItem> pendingDoses = store.pendingDoses(dateKey(selectedDay));
        if (pendingDoses.isEmpty()) {
            root.addView(cardText("Tamamlandı", "Bu gün için bekleyen ilaç kalmadı."));
        }
        for (DoseItem dose : pendingDoses) {
            root.addView(doseRow(dose, true));
        }

        root.addView(sectionLabel("Bildirimler"));
        for (AlertItem alert : store.lastAlerts()) {
            root.addView(alertRow(alert));
        }

        add.setOnClickListener(v -> showAddMedicationDialog());
        symptom.setOnClickListener(v -> showSymptomDialog());
        emergency.setOnClickListener(v -> confirmEmergency());
        setContentView(wrap(root));
    }

    private void showCaregiverHome() {
        LinearLayout root = page();
        root.addView(topBar("Sorumlu Paneli", currentUser.fullName));
        root.addView(connectionPill(store.patientCode));
        root.addView(treatmentSummaryCard());
        root.addView(cardText("Bağlı hasta", store.patient.fullName + "\nBu hafta nöbetçi: " + store.dutyCaregiver()));
        root.addView(calendarCard());

        LinearLayout actions = horizontal();
        Button add = primary("Hastaya İlaç Ekle");
        Button missed = outline("Kaçırılan Doz");
        actions.addView(add, weight());
        actions.addView(missed, weight());
        root.addView(actions);

        root.addView(sectionLabel(selectedDayLabel() + " ilaç planı"));
        for (DoseItem dose : store.scheduledDoses(dateKey(selectedDay))) {
            root.addView(doseRow(dose, false));
        }

        root.addView(sectionLabel("Sorumlu bildirim günlüğü"));
        for (AlertItem alert : store.lastAlerts()) {
            root.addView(alertRow(alert));
        }

        add.setOnClickListener(v -> showAddMedicationDialog());
        missed.setOnClickListener(v -> showMissedDoseDialog());
        setContentView(wrap(root));
    }

    private View topBar(String heading, String name) {
        LinearLayout box = vertical();
        box.setPadding(0, dp(8), 0, dp(12));
        TextView h = title(heading);
        LinearLayout row = horizontal();
        TextView n = body(name);
        Button profile = smallButton("Profil");
        Button logout = smallButton("Çıkış");
        row.addView(n, weight());
        row.addView(profile, weight());
        row.addView(logout, weight());
        box.addView(h);
        box.addView(row);
        profile.setOnClickListener(v -> showProfile());
        logout.setOnClickListener(v -> showLogin());
        return box;
    }

    private void showProfile() {
        if ("Hasta".equals(currentUser.role)) {
            showPatientProfile();
        } else {
            showCaregiverProfile();
        }
    }

    private void showPatientProfile() {
        LinearLayout root = page();
        root.addView(profileHeader("Hasta Profili", store.patient.fullName));
        root.addView(cardText("Genel bilgiler",
                "Yaş: " + store.patient.age + "\nKan grubu: " + store.patient.bloodType + "\nBağlantı kodu: " + store.patientCode));
        root.addView(listCard("Rahatsızlıklar", store.patient.conditions));
        root.addView(listCard("Alerjiler ve hassasiyetler", store.patient.allergies));
        root.addView(cardText("Ev ve güvenlik",
                "Ev konumu: " + store.patient.homeAddress + "\nGeofence yarıçapı: 150 m\nOnay bekleme süresi: 15 dakika"));
        root.addView(listCard("Bağlı sorumlular", Arrays.asList("Mehmet Demir - bu hafta nöbetçi", "Elif Kaya - yedek sorumlu")));
        root.addView(listCard("Doktor notları", store.patient.doctorNotes));
        setContentView(wrap(root));
    }

    private void showCaregiverProfile() {
        LinearLayout root = page();
        root.addView(profileHeader("Sorumlu Profili", currentUser.fullName));
        root.addView(cardText("Hesap bilgileri",
                "Rol: Sorumlu\nE-posta: " + currentUser.email + "\nAktif hasta: " + store.patient.fullName));
        root.addView(cardText("Nöbet ve erişim",
                "Bu hafta nöbetçi: " + store.dutyCaregiver() + "\nRutin uyarılar nöbetçi sorumluya gider.\nACİL bildirimi tüm sorumlulara gider."));
        root.addView(listCard("Hasta sağlık özeti - " + store.patient.fullName, store.patient.conditions));
        root.addView(listCard("Takip yetkileri", Arrays.asList(
                "İlaç ekleme ve çıkarma",
                "Düşük stok ve kaçırılan doz uyarılarını görme",
                "Semptom kayıtlarını takip etme",
                "Acil durum bildirimlerini alma"
        )));
        root.addView(listCard("Yakın dönem notları", Arrays.asList(
                "Glifor stoğu düşük; reçete yenileme kontrol edilmeli.",
                "Son semptom kaydı mide bulantısı olarak işaretlendi.",
                "Akşam ilaçları için evden çıkış hatırlatması aktif."
        )));
        setContentView(wrap(root));
    }

    private View profileHeader(String heading, String name) {
        LinearLayout box = vertical();
        box.setPadding(0, dp(8), 0, dp(10));
        box.addView(title(heading));
        box.addView(body(name));
        Button back = outline("Panele Dön");
        box.addView(back);
        back.setOnClickListener(v -> showHome());
        return box;
    }

    private View treatmentSummaryCard() {
        LinearLayout card = card();
        card.addView(label("BUGÜNKÜ TEDAVİ ÖZETİ"));
        card.addView(summaryLine("Seçili gün", selectedDayLabel() + " için " + store.scheduledDoses(dateKey(selectedDay)).size() + " doz planlandı."));
        card.addView(summaryLine("Bekleyen dozlar", store.pendingDoseSummary(dateKey(selectedDay))));
        card.addView(summaryLine("Tamamlanan dozlar", store.completedDoseSummary(dateKey(selectedDay))));
        card.addView(summaryLine("Stok", store.lowStockCount() + " ilaç düşük stokta. Glifor için eczane/reçete kontrolü önerilir."));
        card.addView(body("Takvimden başka bir güne basarak o günün ilaç planını görüntüleyebilirsiniz."));
        Medication low = store.lowestStockMedication();
        if (low != null) {
            card.addView(chip("Öncelik: " + low.name + " stoğu " + low.stock, low.stock <= low.threshold ? RED : GREEN));
        }
        return card;
    }

    private View summaryLine(String title, String text) {
        LinearLayout row = vertical();
        row.setPadding(0, dp(5), 0, dp(3));
        TextView h = new TextView(this);
        h.setText(title);
        h.setTextColor(TEXT);
        h.setTextSize(15);
        h.setTypeface(Typeface.DEFAULT_BOLD);
        TextView b = body(text);
        b.setPadding(0, dp(1), 0, dp(2));
        row.addView(h);
        row.addView(b);
        return row;
    }

    private View metricBox(String labelText, String value, int color) {
        LinearLayout box = vertical();
        box.setPadding(dp(8), dp(8), dp(8), dp(8));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(248, 250, 253));
        bg.setCornerRadius(dp(8));
        bg.setStroke(1, Color.rgb(226, 231, 238));
        box.setBackground(bg);

        TextView valueText = new TextView(this);
        valueText.setText(value);
        valueText.setTextColor(color);
        valueText.setTextSize(18);
        valueText.setTypeface(Typeface.DEFAULT_BOLD);
        valueText.setGravity(Gravity.CENTER);

        TextView label = new TextView(this);
        label.setText(labelText);
        label.setTextColor(MUTED);
        label.setTextSize(11);
        label.setGravity(Gravity.CENTER);

        box.addView(valueText);
        box.addView(label);
        return box;
    }

    private View calendarCard() {
        LinearLayout card = card();
        LinearLayout header = horizontal();
        Button prev = navButton("‹");
        Button next = navButton("›");
        TextView month = new TextView(this);
        month.setText(monthName() + "\nSeçili gün: " + selectedDayLabel());
        month.setTextColor(TEXT);
        month.setTextSize(16);
        month.setTypeface(Typeface.DEFAULT_BOLD);
        month.setGravity(Gravity.CENTER);
        header.addView(prev);
        header.addView(month, weight());
        header.addView(next);
        card.addView(header);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(7);
        String[] days = {"Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz"};
        for (String day : days) {
            TextView tv = calendarCell(day, false, false, false);
            tv.setTextColor(MUTED);
            grid.addView(tv);
        }
        Calendar c = (Calendar) visibleMonth.clone();
        int max = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= max; i++) {
            Calendar cellDate = (Calendar) visibleMonth.clone();
            cellDate.set(Calendar.DAY_OF_MONTH, i);
            boolean planned = !store.scheduledDoses(dateKey(cellDate)).isEmpty();
            boolean today = sameMonthToday() && i == Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            boolean selected = isSameDay(cellDate, selectedDay);
            TextView cell = calendarCell(String.valueOf(i), planned, today, selected);
            final Calendar selectedDate = (Calendar) cellDate.clone();
            cell.setOnClickListener(v -> {
                selectedDay = selectedDate;
                visibleMonth = (Calendar) selectedDate.clone();
                toast(selectedDayLabel() + " ilaç planı açıldı.");
                showHome();
            });
            grid.addView(cell);
        }
        card.addView(grid);

        prev.setOnClickListener(v -> {
            visibleMonth.add(Calendar.MONTH, -1);
            showHome();
        });
        next.setOnClickListener(v -> {
            visibleMonth.add(Calendar.MONTH, 1);
            showHome();
        });
        return card;
    }

    private View doseRow(DoseItem dose, boolean patientControls) {
        Medication med = dose.medication;
        LinearLayout row = card();
        LinearLayout top = horizontal();
        TextView name = new TextView(this);
        name.setText(dose.time + "  •  " + med.name + "  " + med.dose);
        name.setTextColor(TEXT);
        name.setTextSize(17);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        TextView stock = chip("Stok: " + med.stock, med.stock <= med.threshold ? RED : GREEN);
        top.addView(name, weight());
        top.addView(stock);
        row.addView(top);
        row.addView(body(med.pattern + "  |  Stok eşiği: " + med.threshold));

        LinearLayout buttons = horizontal();
        Button confirm = smallButton("Aldım");
        Button remove = smallButton("Çıkar");
        if (patientControls) {
            buttons.addView(confirm, weight());
        }
        buttons.addView(remove, weight());
        row.addView(buttons);

        confirm.setOnClickListener(v -> {
            store.confirmDose(dateKey(selectedDay), dose);
            String message = dose.time + " " + med.name + " dozu onaylandı. Kalan stok: " + med.stock;
            store.alerts.add(0, new AlertItem("Doz onayı", message, now()));
            if (med.stock <= med.threshold) {
                store.alerts.add(0, new AlertItem("Düşük stok", med.name + " stoğu eşik değerinin altına düştü.", now()));
            }
            toast(message);
            showHome();
        });
        remove.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("İlaç çıkarılsın mı?")
                .setMessage(med.name + " hasta listesinden pasif hale getirilecek.")
                .setNegativeButton("Vazgeç", null)
                .setPositiveButton("Çıkar", (d, which) -> {
                    med.active = false;
                    store.alerts.add(0, new AlertItem("İlaç çıkarıldı", med.name + " tedavi listesinden kaldırıldı.", now()));
                    showHome();
                }).show());
        return row;
    }

    private void showAddMedicationDialog() {
        LinearLayout form = vertical();
        form.setPadding(dp(16), dp(10), dp(16), 0);
        Spinner name = spinner(store.catalogNames());
        EditText stock = input("Stok adedi");
        stock.setInputType(InputType.TYPE_CLASS_NUMBER);
        Spinner pattern = spinner(Arrays.asList("Sabah", "Öğle", "Akşam", "Sabah - Akşam (Günde 2)", "Sabah - Öğle - Akşam (Günde 3)"));
        form.addView(label("İLAÇ İSMİ"));
        form.addView(name);
        form.addView(label("STOK"));
        form.addView(stock);
        form.addView(label("KULLANIM ŞEKLİ"));
        form.addView(pattern);

        new AlertDialog.Builder(this)
                .setTitle("İlaç Ekle")
                .setView(form)
                .setNegativeButton("Vazgeç", null)
                .setPositiveButton("İlaç Ekle", (dialog, which) -> {
                    int s = parseInt(stock.getText().toString(), 20);
                    String medName = String.valueOf(name.getSelectedItem());
                    store.medications.add(0, new Medication(medName, "1 tablet", String.valueOf(pattern.getSelectedItem()), s, 5, true));
                    store.alerts.add(0, new AlertItem("İlaç eklendi", medName + " hasta takvimine eklendi.", now()));
                    showHome();
                }).show();
    }

    private void showSymptomDialog() {
        if (store.activeMeds().isEmpty()) {
            toast("Semptom eklemek için önce ilaç ekleyin.");
            return;
        }
        LinearLayout form = vertical();
        form.setPadding(dp(16), dp(10), dp(16), 0);
        Spinner med = spinner(store.activeMedNames());
        Spinner symptom = spinner(Arrays.asList("Baş dönmesi", "Mide bulantısı", "Karın ağrısı", "Baş ağrısı", "Kaşıntı", "Çarpıntı"));
        Spinner duration = spinner(Arrays.asList("Aynı anda", "30 dakika sonra", "1 saat sonra", "2 saat sonra", "3 saat sonra"));
        TextView pattern = body("Kullanım şekli: " + store.findActive(String.valueOf(med.getSelectedItem())).pattern);
        med.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Medication selected = store.findActive(String.valueOf(parent.getItemAtPosition(position)));
                pattern.setText("Kullanım şekli: " + selected.pattern);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        form.addView(label("İLAÇ İSMİ"));
        form.addView(med);
        form.addView(label("Semptom Türü"));
        form.addView(symptom);
        form.addView(pattern);
        form.addView(label("Süre"));
        form.addView(duration);

        new AlertDialog.Builder(this)
                .setTitle("Semptom Ekle")
                .setView(form)
                .setNegativeButton("Vazgeç", null)
                .setPositiveButton("Semptom Ekle", (dialog, which) -> {
                    String body = "Ayşe Demir " + med.getSelectedItem() + " ilacından sonra " + symptom.getSelectedItem() + " semptomu ekledi.";
                    store.alerts.add(0, new AlertItem("Semptom", body + " Süre: " + duration.getSelectedItem(), now()));
                    toast("Semptom başarıyla eklendi.");
                    showHome();
                }).show();
    }

    private void confirmEmergency() {
        new AlertDialog.Builder(this)
                .setTitle("Acil durum bildirimi")
                .setMessage("Acil durum bildirimi tüm sorumlu hesaplara gönderilecektir. Onaylıyor musunuz?")
                .setNegativeButton("Vazgeç", null)
                .setPositiveButton("Gönder", (dialog, which) -> {
                    store.alerts.add(0, new AlertItem("ACİL", "Ayşe Demir acil yardım bildirimi gönderdi. Konum: Ev çevresi, " + now(), now()));
                    toast("Acil bildirim gönderildi.");
                    showPatientHome();
                }).show();
    }

    private void showMissedDoseDialog() {
        LinearLayout list = vertical();
        list.setPadding(dp(16), dp(8), dp(16), 0);
        for (AlertItem alert : store.missedDoseAlerts()) {
            LinearLayout row = vertical();
            row.setPadding(0, dp(8), 0, dp(8));
            TextView title = new TextView(this);
            title.setText(alert.title + "  •  " + alert.time);
            title.setTextColor(RED);
            title.setTextSize(15);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            row.addView(title);
            row.addView(body(alert.body));
            list.addView(row);
        }

        new AlertDialog.Builder(this)
                .setTitle("Kaçırılan Doz Bildirimleri")
                .setMessage("Ayşe Demir için sistemde kayıtlı kaçırılan ilaç bildirimleri")
                .setView(list)
                .setPositiveButton("Tamam", null)
                .show();
    }

    private View alertRow(AlertItem alert) {
        LinearLayout row = card();
        TextView title = new TextView(this);
        title.setText(alert.title + "  •  " + alert.time);
        title.setTextColor("ACİL".equals(alert.title) ? RED : TEXT);
        title.setTextSize(16);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        row.addView(title);
        row.addView(body(alert.body));
        return row;
    }

    private View cardText(String h, String b) {
        LinearLayout row = card();
        TextView ht = label(h);
        row.addView(ht);
        row.addView(body(b));
        return row;
    }

    private View listCard(String h, List<String> items) {
        LinearLayout row = card();
        row.addView(label(h));
        for (String item : items) {
            row.addView(bullet(item));
        }
        return row;
    }

    private TextView connectionPill(String code) {
        TextView pill = chip("Bağlantı Kodu: " + code, BLUE);
        pill.setTextSize(16);
        pill.setGravity(Gravity.CENTER);
        pill.setOnClickListener(v -> toast("Kod kopyalandı: " + code));
        return pill;
    }

    private LinearLayout page() {
        LinearLayout root = vertical();
        root.setPadding(dp(18), dp(20), dp(18), dp(28));
        root.setBackgroundColor(BG);
        return root;
    }

    private ScrollView wrap(LinearLayout root) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(root);
        return scroll;
    }

    private LinearLayout vertical() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout horizontal() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        l.setPadding(0, dp(4), 0, dp(4));
        return l;
    }

    private LinearLayout card() {
        LinearLayout l = vertical();
        l.setPadding(dp(14), dp(14), dp(14), dp(14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(8), 0, dp(8));
        l.setLayoutParams(lp);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(CARD);
        bg.setCornerRadius(dp(8));
        bg.setStroke(1, Color.rgb(226, 231, 238));
        l.setBackground(bg);
        return l;
    }

    private TextView title(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(TEXT);
        t.setTextSize(28);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(0, dp(6), 0, dp(4));
        return t;
    }

    private TextView sectionLabel(String text) {
        TextView t = label(text);
        t.setPadding(0, dp(18), 0, dp(6));
        return t;
    }

    private TextView label(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(MUTED);
        t.setTextSize(13);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private TextView body(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(MUTED);
        t.setTextSize(15);
        t.setLineSpacing(4, 1);
        t.setPadding(0, dp(2), 0, dp(8));
        return t;
    }

    private TextView bullet(String text) {
        TextView t = body("• " + text);
        t.setPadding(0, dp(2), 0, dp(2));
        return t;
    }

    private TextView chip(String text, int color) {
        TextView c = new TextView(this);
        c.setText(text);
        c.setTextColor(Color.WHITE);
        c.setTextSize(13);
        c.setTypeface(Typeface.DEFAULT_BOLD);
        c.setPadding(dp(10), dp(6), dp(10), dp(6));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(18));
        c.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMargins(0, dp(4), 0, dp(8));
        c.setLayoutParams(lp);
        return c;
    }

    private Button primary(String text) {
        Button b = button(text, Color.rgb(20, 24, 31), Color.WHITE);
        return b;
    }

    private Button outline(String text) {
        return button(text, Color.WHITE, TEXT);
    }

    private Button danger(String text) {
        Button b = button(text, RED, Color.WHITE);
        b.setTextSize(22);
        return b;
    }

    private Button smallButton(String text) {
        Button b = button(text, Color.WHITE, TEXT);
        b.setTextSize(14);
        return b;
    }

    private Button navButton(String text) {
        Button b = button(text, Color.WHITE, TEXT);
        b.setTextSize(18);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(46), dp(40));
        lp.setMargins(dp(4), 0, dp(4), 0);
        b.setLayoutParams(lp);
        return b;
    }

    private Button button(String text, int bgColor, int textColor) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(textColor);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(bgColor);
        bg.setCornerRadius(dp(8));
        bg.setStroke(1, Color.rgb(210, 218, 230));
        b.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(48));
        lp.setMargins(0, dp(6), 0, dp(6));
        b.setLayoutParams(lp);
        return b;
    }

    private EditText input(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setSingleLine(true);
        e.setTextSize(16);
        e.setPadding(dp(12), 0, dp(12), 0);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(8));
        bg.setStroke(1, Color.rgb(210, 218, 230));
        e.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(52));
        lp.setMargins(0, dp(6), 0, dp(6));
        e.setLayoutParams(lp);
        return e;
    }

    private Spinner spinner(List<String> values) {
        Spinner s = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, values);
        s.setAdapter(adapter);
        return s;
    }

    private TextView calendarCell(String text, boolean planned, boolean today, boolean selected) {
        TextView cell = new TextView(this);
        cell.setText(text);
        cell.setTextSize(13);
        cell.setGravity(Gravity.CENTER);
        cell.setTextColor(today || selected ? Color.WHITE : TEXT);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(8));
        bg.setColor(selected ? Color.rgb(20, 24, 31) : today ? BLUE : planned ? Color.rgb(231, 242, 255) : Color.TRANSPARENT);
        cell.setBackground(bg);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = dp(44);
        lp.height = dp(38);
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        cell.setLayoutParams(lp);
        return cell;
    }

    private View space(int h) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(h)));
        return v;
    }

    private LinearLayout.LayoutParams weight() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1);
        lp.setMargins(dp(4), 0, dp(4), 0);
        return lp;
    }

    private String monthName() {
        SimpleDateFormat fmt = new SimpleDateFormat("MMMM yyyy", new Locale("tr", "TR"));
        return fmt.format(visibleMonth.getTime());
    }

    private boolean sameMonthToday() {
        Calendar today = Calendar.getInstance();
        return today.get(Calendar.MONTH) == visibleMonth.get(Calendar.MONTH)
                && today.get(Calendar.YEAR) == visibleMonth.get(Calendar.YEAR);
    }

    private String now() {
        return new SimpleDateFormat("HH:mm", new Locale("tr", "TR")).format(new Date());
    }

    private String selectedDayLabel() {
        return new SimpleDateFormat("d MMMM", new Locale("tr", "TR")).format(selectedDay.getTime());
    }

    private String dateKey(Calendar day) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(day.getTime());
    }

    private boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.MONTH) == b.get(Calendar.MONTH)
                && a.get(Calendar.DAY_OF_MONTH) == b.get(Calendar.DAY_OF_MONTH);
    }

    private int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return fallback;
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    static class User {
        final String fullName;
        final String email;
        final String password;
        final String role;

        User(String fullName, String email, String password, String role) {
            this.fullName = fullName;
            this.email = email;
            this.password = password;
            this.role = role;
        }
    }

    static class Medication {
        final String name;
        final String dose;
        final String pattern;
        int stock;
        final int threshold;
        boolean active;

        Medication(String name, String dose, String pattern, int stock, int threshold, boolean active) {
            this.name = name;
            this.dose = dose;
            this.pattern = pattern;
            this.stock = stock;
            this.threshold = threshold;
            this.active = active;
        }
    }

    static class DoseItem {
        final Medication medication;
        final String time;

        DoseItem(Medication medication, String time) {
            this.medication = medication;
            this.time = time;
        }

        String key(String dateKey) {
            return dateKey + "|" + medication.name + "|" + time;
        }
    }

    static class AlertItem {
        final String title;
        final String body;
        final String time;

        AlertItem(String title, String body, String time) {
            this.title = title;
            this.body = body;
            this.time = time;
        }
    }

    static class PatientInfo {
        final String fullName;
        final int age;
        final String bloodType;
        final String homeAddress;
        final List<String> conditions;
        final List<String> allergies;
        final List<String> doctorNotes;

        PatientInfo(String fullName, int age, String bloodType, String homeAddress,
                    List<String> conditions, List<String> allergies, List<String> doctorNotes) {
            this.fullName = fullName;
            this.age = age;
            this.bloodType = bloodType;
            this.homeAddress = homeAddress;
            this.conditions = conditions;
            this.allergies = allergies;
            this.doctorNotes = doctorNotes;
        }
    }

    static class DemoStore {
        final String patientCode = "QWERTY123";
        final PatientInfo patient = new PatientInfo(
                "Ayşe Demir",
                68,
                "A Rh+",
                "Kadıköy / İstanbul",
                Arrays.asList("Tip 2 diyabet", "Hipertansiyon", "Kolesterol yüksekliği", "Reflü"),
                Arrays.asList("Penisilin hassasiyeti", "Laktoz intoleransı", "Aspirin yüksek dozda mide rahatsızlığı"),
                Arrays.asList("Kan şekeri sabah ölçümleri takip edilmeli.", "Tansiyon 140/90 üstüne çıkarsa sorumlu bilgilendirilmeli.", "Mide bulantısı tekrarlarsa doktor kontrolü önerilir.")
        );
        final List<User> users = new ArrayList<>();
        final List<Medication> medications = new ArrayList<>();
        final List<String> catalog = new ArrayList<>();
        final List<AlertItem> alerts = new ArrayList<>();
        final Set<String> completedDoseKeys = new HashSet<>();

        DemoStore() {
            users.add(new User("Ayşe Demir", "hasta.demo@ilac.app", "123456", "Hasta"));
            users.add(new User("Mehmet Demir", "sorumlu1.demo@ilac.app", "123456", "Sorumlu"));
            users.add(new User("Elif Kaya", "sorumlu2.demo@ilac.app", "123456", "Sorumlu"));

            catalog.addAll(Arrays.asList(
                    "Parol", "Apranax", "Coraspin", "Beloc Zok", "Glifor", "Lansor",
                    "Nexium", "Tylolhot", "Minoset", "Majezik", "Arveles", "Dolorex",
                    "Augmentin", "Klacid", "Cipro", "Aferin", "Ventolin", "Pulmicort",
                    "Forziga", "Diaformin", "Euthyrox", "Crestor", "Lipitor", "Vasoxen",
                    "Norvasc", "Delix", "Coumadin", "Xarelto", "Dideral", "Zyrtec"
            ));

            medications.add(new Medication("Coraspin", "100 mg", "Sabah", 18, 5, true));
            medications.add(new Medication("Beloc Zok", "50 mg", "Sabah - Akşam (Günde 2)", 9, 5, true));
            medications.add(new Medication("Glifor", "1000 mg", "Sabah - Öğle - Akşam (Günde 3)", 4, 5, true));
            medications.add(new Medication("Lansor", "30 mg", "Sabah", 11, 5, true));
            medications.add(new Medication("Euthyrox", "25 mcg", "Sabah", 28, 5, true));

            alerts.add(new AlertItem("Kaçırılan doz", "Ayşe Demir 18:00 Glifor 1000 mg dozunu 15 dakika içinde onaylamadı.", "Bugün 18:16"));
            alerts.add(new AlertItem("Kaçırılan doz", "Ayşe Demir 13:00 Glifor 1000 mg dozunu kaçırdı. Sorumlu kontrolü önerilir.", "Dün 13:18"));
            alerts.add(new AlertItem("Kaçırılan doz", "Ayşe Demir 08:00 Beloc Zok 50 mg dozunu onaylamadı.", "Pazartesi 08:17"));
            alerts.add(new AlertItem("Düşük stok", "Glifor stoğu 4 adede düştü. Varsayılan eşik: 5.", "09:12"));
            alerts.add(new AlertItem("Semptom", "Ayşe Demir Apranax ilacından sonra mide bulantısı semptomu ekledi.", "Dün"));
            alerts.add(new AlertItem("Hatırlatma", "Evden çıkış algılandı; akşam ilaçları yanında olmayabilir.", "Dün"));
        }

        User login(String email, String password) {
            for (User user : users) {
                if (user.email.equalsIgnoreCase(email) && user.password.equals(password)) {
                    return user;
                }
            }
            return null;
        }

        List<Medication> activeMeds() {
            List<Medication> result = new ArrayList<>();
            for (Medication med : medications) {
                if (med.active) result.add(med);
            }
            return result;
        }

        List<DoseItem> scheduledDoses(String dateKey) {
            List<DoseItem> result = new ArrayList<>();
            for (Medication med : activeMeds()) {
                for (String time : doseTimes(med.pattern)) {
                    result.add(new DoseItem(med, time));
                }
            }
            return result;
        }

        List<DoseItem> pendingDoses(String dateKey) {
            List<DoseItem> result = new ArrayList<>();
            for (DoseItem dose : scheduledDoses(dateKey)) {
                if (!completedDoseKeys.contains(dose.key(dateKey))) {
                    result.add(dose);
                }
            }
            return result;
        }

        void confirmDose(String dateKey, DoseItem dose) {
            String key = dose.key(dateKey);
            if (!completedDoseKeys.contains(key)) {
                completedDoseKeys.add(key);
                if (dose.medication.stock > 0) {
                    dose.medication.stock--;
                }
            }
        }

        List<String> doseTimes(String pattern) {
            if (pattern.contains("Sabah - Öğle - Akşam")) {
                return Arrays.asList("08:00", "13:00", "18:00");
            }
            if (pattern.contains("Sabah - Akşam")) {
                return Arrays.asList("08:00", "18:00");
            }
            if (pattern.contains("Öğle")) {
                return Arrays.asList("13:00");
            }
            if (pattern.contains("Akşam")) {
                return Arrays.asList("18:00");
            }
            return Arrays.asList("08:00");
        }

        String pendingDoseSummary(String dateKey) {
            List<DoseItem> doses = pendingDoses(dateKey);
            if (doses.isEmpty()) {
                return "Bekleyen doz kalmadı.";
            }
            List<String> parts = new ArrayList<>();
            for (DoseItem dose : doses) {
                parts.add(dose.time + " " + dose.medication.name);
            }
            return join(parts, ", ");
        }

        String completedDoseSummary(String dateKey) {
            List<String> parts = new ArrayList<>();
            for (DoseItem dose : scheduledDoses(dateKey)) {
                if (completedDoseKeys.contains(dose.key(dateKey))) {
                    parts.add(dose.time + " " + dose.medication.name);
                }
            }
            if (parts.isEmpty()) {
                return "Henüz tamamlanan doz yok.";
            }
            return join(parts, ", ");
        }

        String join(List<String> values, String separator) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) builder.append(separator);
                builder.append(values.get(i));
            }
            return builder.toString();
        }

        List<String> activeMedNames() {
            List<String> result = new ArrayList<>();
            for (Medication med : activeMeds()) result.add(med.name);
            return result;
        }

        Medication findActive(String name) {
            for (Medication med : medications) {
                if (med.active && med.name.equals(name)) return med;
            }
            return medications.get(0);
        }

        List<String> catalogNames() {
            return new ArrayList<>(catalog);
        }

        List<AlertItem> lastAlerts() {
            return alerts.subList(0, Math.min(alerts.size(), 8));
        }

        List<AlertItem> missedDoseAlerts() {
            List<AlertItem> result = new ArrayList<>();
            for (AlertItem alert : alerts) {
                if ("Kaçırılan doz".equals(alert.title)) {
                    result.add(alert);
                }
            }
            return result;
        }

        String dutyCaregiver() {
            return "Mehmet Demir";
        }

        int lowStockCount() {
            int count = 0;
            for (Medication med : activeMeds()) {
                if (med.stock <= med.threshold) count++;
            }
            return count;
        }

        Medication lowestStockMedication() {
            Medication lowest = null;
            for (Medication med : activeMeds()) {
                if (lowest == null || med.stock < lowest.stock) {
                    lowest = med;
                }
            }
            return lowest;
        }

        String nextDoseTime() {
            return "18:00";
        }

        String dailyAdvice() {
            if (lowStockCount() > 0) {
                return "Düşük stoklu ilaçlar için reçete veya eczane kontrolü yapılmalı.";
            }
            return "Bugünkü doz planı düzenli görünüyor; akşam dozu için hatırlatma aktif.";
        }
    }
}

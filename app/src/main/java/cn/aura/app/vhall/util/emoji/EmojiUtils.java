package cn.aura.app.vhall.util.emoji;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.aura.app.R;
import cn.aura.app.vhall.util.VhallUtil;

public class EmojiUtils {
    public static final String f000 = "[微笑]";
    public static final String f001 = "[撇嘴]";
    public static final String f002 = "[色]";
    public static final String f003 = "[发呆]";
    public static final String f004 = "[得意]";
    public static final String f005 = "[流泪]";
    public static final String f006 = "[害羞]";
    public static final String f007 = "[闭嘴]";
    public static final String f008 = "[睡]";
    public static final String f009 = "[哭]";
    public static final String f010 = "[尴尬]";
    public static final String f011 = "[发怒]";
    public static final String f012 = "[调皮]";
    public static final String f013 = "[呲牙]";
    public static final String f014 = "[惊讶]";
    public static final String f015 = "[难过]";
    public static final String f016 = "[酷]";
    public static final String f017 = "[汗]";
    public static final String f018 = "[抓狂]";
    public static final String f019 = "[吐]";
    public static final String f020 = "[偷笑]";
    public static final String f021 = "[愉快]";
    public static final String f022 = "[白眼]";
    public static final String f023 = "[傲慢]";
    public static final String f024 = "[饥饿]";
    public static final String f025 = "[困]";
    public static final String f026 = "[惊恐]";
    public static final String f027 = "[流汗]";
    public static final String f028 = "[憨笑]";
    public static final String f029 = "[悠闲]";
    public static final String f030 = "[奋斗]";
    public static final String f031 = "[咒骂]";
    public static final String f032 = "[疑问]";
    public static final String f033 = "[嘘]";
    public static final String f034 = "[晕]";
    public static final String f035 = "[疯了]";
    public static final String f036 = "[衰]";
    public static final String f037 = "[骷髅]";
    public static final String f038 = "[敲打]";
    public static final String f039 = "[再见]";
    public static final String f040 = "[擦汗]";
    public static final String f041 = "[抠鼻]";
    public static final String f042 = "[鼓掌]";
    public static final String f043 = "[糗大了]";
    public static final String f044 = "[坏笑]";
    public static final String f045 = "[左哼哼]";
    public static final String f046 = "[右哼哼]";
    public static final String f047 = "[哈欠]";
    public static final String f048 = "[鄙视]";
    public static final String f049 = "[委屈]";
    public static final String f050 = "[快哭了]";
    public static final String f051 = "[阴险]";
    public static final String f052 = "[亲亲]";
    public static final String f053 = "[吓]";
    public static final String f054 = "[可怜]";
    public static final String f055 = "[菜刀]";
    public static final String f056 = "[西瓜]";
    public static final String f057 = "[啤酒]";
    public static final String f058 = "[篮球]";
    public static final String f059 = "[乒乓]";
    public static final String f060 = "[咖啡]";
    public static final String f061 = "[饭]";
    public static final String f062 = "[猪头]";
    public static final String f063 = "[玫瑰]";
    public static final String f064 = "[凋谢]";
    public static final String f065 = "[嘴唇]";
    public static final String f066 = "[爱心]";
    public static final String f067 = "[心碎]";
    public static final String f068 = "[蛋糕]";
    public static final String f069 = "[闪电]";
    public static final String f070 = "[炸弹]";
    public static final String f071 = "[刀]";
    public static final String f072 = "[足球]";
    public static final String f073 = "[瓢虫]";
    public static final String f074 = "[便便]";
    public static final String f075 = "[月亮]";
    public static final String f076 = "[太阳]";
    public static final String f077 = "[礼物]";
    public static final String f078 = "[拥抱]";
    public static final String f079 = "[强]";
    public static final String f080 = "[弱]";
    public static final String f081 = "[握手]";
    public static final String f082 = "[胜利]";
    public static final String f083 = "[抱拳]";
    public static final String f084 = "[勾引]";
    public static final String f085 = "[拳头]";
    public static final String f086 = "[差劲]";
    public static final String f087 = "[爱你]";
    public static final String f088 = "[NO]";
    public static final String f089 = "[OK]";
    private static final String filePath = "cn.aura.app.vhall.util.emoji.EmojiUtils";
    private static final String delete_name = "delete_expression";
    private static final Factory spannableFactory = Factory.getInstance();
    private static final Map<Pattern, Integer> emoticons = new HashMap<>();

    static {
        addPattern(f000, R.mipmap.f000);
        addPattern(f001, R.mipmap.f001);
        addPattern(f002, R.mipmap.f002);
        addPattern(f003, R.mipmap.f003);
        addPattern(f004, R.mipmap.f004);
        addPattern(f005, R.mipmap.f005);
        addPattern(f006, R.mipmap.f006);
        addPattern(f007, R.mipmap.f007);
        addPattern(f008, R.mipmap.f008);
        addPattern(f009, R.mipmap.f009);
        addPattern(f010, R.mipmap.f010);
        addPattern(f011, R.mipmap.f011);
        addPattern(f012, R.mipmap.f012);
        addPattern(f013, R.mipmap.f013);
        addPattern(f014, R.mipmap.f014);
        addPattern(f015, R.mipmap.f015);
        addPattern(f016, R.mipmap.f016);
        addPattern(f017, R.mipmap.f017);
        addPattern(f018, R.mipmap.f018);
        addPattern(f019, R.mipmap.f019);

        addPattern(f020, R.mipmap.f020);
        addPattern(f021, R.mipmap.f021);
        addPattern(f022, R.mipmap.f022);
        addPattern(f023, R.mipmap.f023);
        addPattern(f024, R.mipmap.f024);
        addPattern(f025, R.mipmap.f025);
        addPattern(f026, R.mipmap.f026);
        addPattern(f027, R.mipmap.f027);
        addPattern(f028, R.mipmap.f028);
        addPattern(f029, R.mipmap.f029);

        addPattern(f030, R.mipmap.f030);
        addPattern(f031, R.mipmap.f031);
        addPattern(f032, R.mipmap.f032);
        addPattern(f033, R.mipmap.f033);
        addPattern(f034, R.mipmap.f034);
        addPattern(f035, R.mipmap.f035);
        addPattern(f036, R.mipmap.f036);
        addPattern(f037, R.mipmap.f037);
        addPattern(f038, R.mipmap.f038);
        addPattern(f039, R.mipmap.f039);

        addPattern(f040, R.mipmap.f040);
        addPattern(f041, R.mipmap.f041);
        addPattern(f042, R.mipmap.f042);
        addPattern(f043, R.mipmap.f043);
        addPattern(f044, R.mipmap.f044);
        addPattern(f045, R.mipmap.f045);
        addPattern(f046, R.mipmap.f046);
        addPattern(f047, R.mipmap.f047);
        addPattern(f048, R.mipmap.f048);
        addPattern(f049, R.mipmap.f049);
        addPattern(f050, R.mipmap.f050);
        addPattern(f051, R.mipmap.f051);
        addPattern(f052, R.mipmap.f052);
        addPattern(f053, R.mipmap.f053);
        addPattern(f054, R.mipmap.f054);
        addPattern(f055, R.mipmap.f055);
        addPattern(f056, R.mipmap.f056);
        addPattern(f057, R.mipmap.f057);
        addPattern(f058, R.mipmap.f058);
        addPattern(f059, R.mipmap.f059);
        addPattern(f060, R.mipmap.f060);
        addPattern(f061, R.mipmap.f061);
        addPattern(f062, R.mipmap.f062);
        addPattern(f063, R.mipmap.f063);
        addPattern(f064, R.mipmap.f064);
        addPattern(f065, R.mipmap.f065);
        addPattern(f066, R.mipmap.f066);
        addPattern(f067, R.mipmap.f067);
        addPattern(f068, R.mipmap.f068);
        addPattern(f069, R.mipmap.f069);
        addPattern(f070, R.mipmap.f070);
        addPattern(f071, R.mipmap.f071);
        addPattern(f072, R.mipmap.f072);
        addPattern(f073, R.mipmap.f073);
        addPattern(f074, R.mipmap.f074);
        addPattern(f078, R.mipmap.f075);
        addPattern(f075, R.mipmap.f076);
        addPattern(f076, R.mipmap.f077);
        addPattern(f077, R.mipmap.f078);
        addPattern(f079, R.mipmap.f079);
        addPattern(f080, R.mipmap.f080);
        addPattern(f081, R.mipmap.f081);
        addPattern(f082, R.mipmap.f082);
        addPattern(f083, R.mipmap.f083);
        addPattern(f084, R.mipmap.f084);
        addPattern(f085, R.mipmap.f085);
        addPattern(f086, R.mipmap.f086);
        addPattern(f087, R.mipmap.f087);
        addPattern(f088, R.mipmap.f088);
        addPattern(f089, R.mipmap.f089);
    }

    private static void addPattern(String smile, int resource) {
        EmojiUtils.emoticons.put(Pattern.compile(Pattern.quote(smile)), resource);
    }

    /**
     * replace existing spannable with smiles
     */
    private static boolean addEmoji(Context context, Spannable spannable) {
        boolean hasChanges = false;
        for (Map.Entry<Pattern, Integer> entry : emoticons.entrySet()) {
            Matcher matcher = entry.getKey().matcher(spannable);
            while (matcher.find()) {
                boolean set = true;
                for (ImageSpan span : spannable.getSpans(matcher.start(), matcher.end(), ImageSpan.class))
                    if (spannable.getSpanStart(span) >= matcher.start() && spannable.getSpanEnd(span) <= matcher.end())
                        spannable.removeSpan(span);
                    else {
                        set = false;
                        break;
                    }
                if (set) {
                    hasChanges = true;
                    spannable.setSpan(new ImageSpan(context, entry.getValue()), matcher.start(), matcher.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        return hasChanges;
    }

    public static Spannable getEmojiText(Context context, CharSequence text) {
        Spannable spannable = spannableFactory.newSpannable(text);
        addEmoji(context, spannable);
        return spannable;
    }

    private static boolean containsKey(String key) {
        boolean b = false;
        for (Map.Entry<Pattern, Integer> entry : emoticons.entrySet()) {
            Matcher matcher = entry.getKey().matcher(key);
            if (matcher.find()) {
                b = true;
                break;
            }
        }

        return b;
    }

    static List<String> getExpressionRes(int getSum) {
        List<String> reslist = new ArrayList<String>();
        for (int x = 0; x < getSum; x++) {
            String filename = String.format("f%03d", x);
            reslist.add(filename);
        }
        return reslist;
    }

    static View getGridChildView(final Context context, int i, List<String> reslist, final EditText et_chat) {
        View view = View.inflate(context, R.layout.vhall_emoji_grid, null);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        int screenWidth = width;

        EmojiGridView gv = (EmojiGridView) view.findViewById(R.id.gv_emoji);
        List<String> list = new ArrayList<String>();
        if (i == 5) {
            list.addAll(reslist.subList((i - 1) * 20, reslist.size()));
        } else {
            list.addAll(reslist.subList((i - 1) * 20, i * 20));
        }
        gv.setSelector(android.R.color.transparent);
        gv.setNumColumns(7);//设置7列
        int spacing = VhallUtil.dp2px(context, 10);
        gv.setPadding(spacing, spacing, spacing, spacing);
        int itemWidth = (screenWidth - spacing * 8) / 7;
        list.add(delete_name);
        final EmojiAdapter expressionAdapter = new EmojiAdapter(context, 1, list, itemWidth);
        gv.setAdapter(expressionAdapter);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String filename = expressionAdapter.getItem(position);
                try {
                    // 文字输入框可见时，才可输入表情
                    if (filename != delete_name) { // 不是删除键，显示表情
                        // 这里用的反射，所以混淆的时候不要混淆SmileUtils这个类
                        Class clz = Class.forName(filePath);
                        Field field = clz.getField(filename);
                        // et_chat_message
                        // .append(EmojiUtils.getSmiledText(context,
                        // (String) field.get(null)));
                        int index = et_chat.getSelectionStart();// 获取光标所在位置
                        CharSequence text = EmojiUtils.getEmojiText(context, (String) field.get(null));
                        Editable edit = et_chat.getEditableText();// 获取EditText的文字
                        if (index < 0 || index >= edit.length()) {
                            edit.append(text);
                        } else {
                            edit.insert(index, text);// 光标所在位置插入文字
                        }
                    } else { // 删除文字或者表情
                        if (!TextUtils.isEmpty(et_chat.getText())) {
                            int selectionStart = et_chat.getSelectionStart();// 获取光标的位置
                            if (selectionStart > 0) {
                                String body = et_chat.getText().toString();
                                String tempStr = body.substring(0, selectionStart);
                                int i = tempStr.lastIndexOf("[");// 获取最后一个表情的位置
                                if (i != -1) {
                                    CharSequence cs = tempStr.substring(i, selectionStart);
                                    if (EmojiUtils.containsKey(cs.toString()))
                                        et_chat.getEditableText().delete(i, selectionStart);
                                    else
                                        et_chat.getEditableText().delete(selectionStart - 1, selectionStart);
                                } else {
                                    et_chat.getEditableText().delete(selectionStart - 1, selectionStart);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return view;
    }
}

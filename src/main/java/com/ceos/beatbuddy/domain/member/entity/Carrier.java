package com.ceos.beatbuddy.domain.member.entity;

import lombok.Getter;

@Getter
public enum Carrier {
    SKT(false, "SKT", "SKT 알뜰폰"),
    KT(false,  "KT",  "KT 알뜰폰"),
    LGT(false, "LGU+", "LGU+ 알뜰폰"); // 내부 표기 LGU+, 다날 전송은 "LGT"

    private final boolean dummy; // 자리맞춤
    private final String display;
    private final String mvnoDisplay;

    Carrier(boolean dummy, String display, String mvnoDisplay) {
        this.dummy = dummy;
        this.display = display;
        this.mvnoDisplay = mvnoDisplay;
    }

    public static Result parse(String telCarrierInput) {
        String v = telCarrierInput.trim();
        boolean mvno = v.contains("알뜰");
        if (v.startsWith("SKT")) return new Result("SKT", mvno);
        if (v.startsWith("KT"))  return new Result("KT",  mvno);
        // LGU+ 표기를 다날 코드 LGT로 변환
        if (v.startsWith("LGU+") || v.startsWith("LGT")) return new Result("LGT", mvno);
        throw new IllegalArgumentException("지원하지 않는 통신사: " + v);
    }

    /**
     * Danal CARRIER 응답을 화면 표시용 형태로 변환
     * @param danalCarrier Danal에서 받은 CARRIER (SKT, SKT_MVNO, KT, KT_MVNO, LGT, LGT_MVNO)
     * @return 화면 표시용 텍스트 (SKT, SKT_알뜰폰, KT, KT_알뜰폰, LGU+, LGU+_알뜰폰)
     */
    public static String convertDanalCarrierToDisplay(String danalCarrier) {
        if (danalCarrier == null || danalCarrier.trim().isEmpty()) {
            return "알 수 없음";
        }

        String carrier = danalCarrier.trim();
        boolean isMvno = carrier.endsWith("_MVNO");

        String baseCarrier;
        if (carrier.startsWith("SKT")) {
            baseCarrier = "SKT";
        } else if (carrier.startsWith("KT")) {
            baseCarrier = "KT";
        } else if (carrier.startsWith("LGT")) {
            baseCarrier = "LGU+"; // LGT를 LGU+로 표시
        } else {
            return danalCarrier; // 알 수 없는 통신사는 원래 값 그대로
        }

        return isMvno ? baseCarrier + "_알뜰폰" : baseCarrier;
    }

    public record Result(String danalCode, boolean mvno) {}
}

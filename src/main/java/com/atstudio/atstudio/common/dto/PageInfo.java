package com.atstudio.atstudio.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PageInfo {

    private int page;
    private int size;
    private int total;
    private int start;
    private int end;
    private boolean prev;
    private boolean next;

    public static PageInfo of(RequestDTO request, int total) {
        return of(request.getPage(), request.getSize(), total, 10);
    }

    public static PageInfo of(int page, int size, int total, int blockSize) {
        int end = (int) (Math.ceil(page / (double) blockSize)) * blockSize;
        int start = end - blockSize + 1;
        int last = (int) (Math.ceil(total / (double) size));
        end = Math.min(end, last);

        return PageInfo.builder()
                .page(page)
                .size(size)
                .total(total)
                .start(start)
                .end(end)
                .prev(start > 1)
                .next(total > end * size)
                .build();
    }
}

/* =========================================================
   외부 발주처 DB 더미 데이터 - MariaDB 기준
   기준: 기존 02_supplier_seed.sql
   대상 DDL: 02_supplier_schema_mariadb.sql
   주요 변경:
   - Oracle DATE literal: DATE 'YYYY-MM-DD' -> 'YYYY-MM-DD'
   - EXTERNAL_ORDER_RECEIPT: RECEIVED_AT/PROCESSED_AT -> CREATED_AT/UPDATED_AT
   - EXTERNAL_SHIPMENT: SHIPPED_AT -> CREATED_AT
   - MariaDB AUTO_INCREMENT PK에 명시 ID 입력 허용
========================================================= */

/* 1. SUPPLIER - 발주처 */
INSERT INTO SUPPLIER (SUPPLIER_ID, SUPPLIER_NAME, SUPPLIER_TYPE, ADDRESS, PHONE_NUMBER,
                      OPERATION_STATUS)
VALUES (601, 'TIME 한섬 본사 발주팀', '본사발주처', '서울특별시 강남구 도산대로 523', '02-3416-3100', 'ACTIVE'),
       (602, 'SYSTEM 한섬 본사 발주팀', '본사발주처', '서울특별시 강남구 도산대로 523', '02-3416-3200', 'ACTIVE'),
       (603, 'MINE 한섬 본사 발주팀', '본사발주처', '서울특별시 강남구 도산대로 523', '02-3416-3300', 'ACTIVE'),
       (604, 'THE CASHMERE 한섬 본사 발주팀', '본사발주처', '서울특별시 강남구 도산대로 523', '02-3416-3400', 'ACTIVE');


/* 2. SUPPLIER_PRODUCT - 발주처상품 */
/* 엑셀에 별도 외부상품ID/공급가 시트가 없어 PRODUCT.상품ID를 SUPPLIER_PRODUCT_ID로 사용하고,
   판매가의 70%를 공급가 더미값으로 산정한다. */
INSERT INTO SUPPLIER_PRODUCT (SUPPLIER_PRODUCT_ID, SUPPLIER_ID, EXTERNAL_PRODUCT_ID, PRODUCT_NAME,
                              SUPPLY_PRICE, PRODUCT_STATUS)
VALUES (501, 601, 'EXT-PROD-501', 'TIME 울 블렌드 재킷', 278600, 'ACTIVE'),
       (502, 601, 'EXT-PROD-502', 'TIME 썸머 린넨 원피스', 187600, 'ACTIVE'),
       (503, 602, 'EXT-PROD-503', 'SYSTEM 캐시미어 니트', 229600, 'ACTIVE'),
       (504, 603, 'EXT-PROD-504', 'MINE 트렌치 코트', 369600, 'ACTIVE'),
       (505, 604, 'EXT-PROD-505', 'THE CASHMERE 홀가먼트 니트', 320600, 'ACTIVE');


/* 3. SUPPLIER_INVENTORY - 발주처재고 */
INSERT INTO SUPPLIER_INVENTORY (SUPPLIER_ID, SUPPLIER_PRODUCT_ID, CURRENT_QUANTITY, UPDATED_AT)
VALUES (601, 501, 100, '2026-05-06 00:00:00'),
       (601, 502, 20, '2026-05-06 00:00:00'),
       (602, 503, 75, '2026-05-06 00:00:00'),
       (603, 504, 40, '2026-05-06 00:00:00'),
       (604, 505, 60, '2026-05-06 00:00:00');


/* 4. EXTERNAL_ORDER_RECEIPT - 외부발주접수
   MariaDB DDL 기준 컬럼명:
   - CREATED_AT: 접수일자 역할
   - UPDATED_AT: 처리일자 역할
*/
INSERT INTO EXTERNAL_ORDER_RECEIPT (EXTERNAL_ORDER_RECEIPT_ID,
                                    SUPPLIER_ID,
                                    SUPPLIER_PRODUCT_ID,
                                    INTERNAL_ORDER_REQUEST_ID,
                                    REQUEST_STORE_NAME,
                                    REQUEST_QUANTITY,
                                    APPROVED_QUANTITY,
                                    RECEIPT_STATUS,
                                    CREATED_AT,
                                    UPDATED_AT)
VALUES (1001, 601, 501, '1', 'TIME 판교점', 20, 20, 'RECEIVED', '2026-05-01 00:00:00',
        '2026-05-01 00:00:00'),
       (1002, 601, 502, '2', 'TIME 판교점', 15, 10, 'SHIPPED', '2026-05-02 11:00:00', '2026-05-02 12:00:00'),
       (1003, 602, 503, '3', 'SYSTEM 무역센터점', 10, 0, 'REJECTED', '2026-05-03 09:00:00', '2026-05-03 09:30:00'),
       (1004, 603, 504, '4', 'MINE 더현대서울점', 12, 12, 'SHIPPED', '2026-05-04 14:00:00', '2026-05-04 15:00:00');


/* 5. EXTERNAL_SHIPMENT - 외부출고처리
   MariaDB DDL 기준 컬럼명:
   - CREATED_AT: 출고일자 역할
*/
INSERT INTO EXTERNAL_SHIPMENT (EXTERNAL_SHIPMENT_ID,
                               EXTERNAL_ORDER_RECEIPT_ID,
                               SHIPMENT_QUANTITY,
                               SHIPMENT_STATUS,
                               CREATED_AT)
VALUES (2001, 1001, 20, 'SHIPPED', '2026-05-03 00:00:00');

COMMIT;

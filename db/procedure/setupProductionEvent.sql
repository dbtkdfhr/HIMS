-- 1. MariaDB 이벤트 스케줄러 활성화
SET GLOBAL event_scheduler = ON;

-- 2. 매일 자정에 생산 로직을 실행하는 이벤트 생성
CREATE EVENT IF NOT EXISTS eventDailyProductionCheck
    ON SCHEDULE EVERY 1 DAY
        STARTS (CURRENT_DATE + INTERVAL 1 DAY)
    COMMENT '매일 재고를 체크하여 999개 이하일 경우 1000개 생산을 자동화'
    DO
    CALL procRestockSupplierInventory();

 -- select * from supplier_inventory;
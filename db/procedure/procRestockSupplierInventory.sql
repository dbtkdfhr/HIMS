DELIMITER //

CREATE PROCEDURE procRestockSupplierInventory()
BEGIN
    -- 현재 재고(CURRENT_QUANTITY)가 999개 이하인 품목에 대해서만 1,000개를 추가 생산(UPDATE)
    UPDATE supplier_inventory
    SET current_quantity = current_quantity +1000,
        updated_at = now()
    WHERE current_quantity <=999;
END //

DELIMITER ;
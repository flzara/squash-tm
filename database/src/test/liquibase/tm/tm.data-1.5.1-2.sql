-- Let's botch some test suite test plans that were created in 1.4.1.sql then migrated to 1.5.x schema

-- First we need to make sure that our initial data are square, 
-- because at this point the test plan order might be random (because the order is arbitrary set
-- when TEST_SUITE_TEST_PLAN_ITEM is created by changeset whatever-the-actual-id).
-- After the data had been altered (see below) and the fix applied, the system should be set back to that state.

update TEST_SUITE_TEST_PLAN_ITEM
set test_plan_order = 0
where tpi_id in (2, 3, 22, 39, 13);

update TEST_SUITE_TEST_PLAN_ITEM
set test_plan_order = 1
where tpi_id in (4, 5, 24, 14);

update TEST_SUITE_TEST_PLAN_ITEM
set test_plan_order = 2
where tpi_id in (8, 6, 28, 20);

update TEST_SUITE_TEST_PLAN_ITEM
set test_plan_order = 3
where tpi_id in (9, 7);

update TEST_SUITE_TEST_PLAN_ITEM
set test_plan_order = 4
where tpi_id in (11, 10);

-- Now we can alter the data. We'll simulate the holes left by deleted test cases by shifting the index of some of the items. 
-- for suite 1, tpi 9 and 11 will be shifted by two on the right
-- for suite 2, tpi 3, 5 and 6 will be shifted by one and 7, 10 by two
-- for suite 5, tpi 20 will be shifted by one.
-- this is done by incrementing one by one the concerned items.

update TEST_SUITE_TEST_PLAN_ITEM 
set test_plan_order = test_plan_order + 1
where tpi_id in (9, 11, 3, 5, 6, 7, 10, 20);

update TEST_SUITE_TEST_PLAN_ITEM
set test_plan_order = test_plan_order + 1
where tpi_id in (9, 11, 7, 10);
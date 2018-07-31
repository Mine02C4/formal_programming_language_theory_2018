COINS_DIR := coins-1.5-en
LIR_OPT := divex/printflow/peephole_cse/printflow/constant_folding/printflow
AV1_LIR_OPT := divex/printflow/av/printflow
AV2_LIR_OPT := divex/printflow/av_cse/printflow

CC := java -classpath $(COINS_DIR)/classes coins.driver.driver
CFLAGS := -I$(COINS_DIR)/lang/c/include -I$(COINS_DIR)/lang/c/include/samples

.PHONY: coins test test_* test/*

coins:
	cd $(COINS_DIR) && ./build.sh

test: test/test.out
	./$<

test_av: test/test_av1.out test/test_av2.out
	./test/test_av1.out | tee test_av1.txt
	./test/test_av2.out | tee test_av2.txt
	diff test_av1.txt test_av2.txt

test/test.log: test/test.out
	$< > $@

test/test.out: test/test.c
	gcc $< -o $@

test/test2.out: test/test.c
	java -classpath $(COINS_DIR)/classes coins.driver.Driver \
		-I$(COINS_DIR)/lang/c/include -I$(COINS_DIR)/lang/c/include/samples \
		$< -coins:assembler=as,target=x86_64,lir-opt=$(LIR_OPT) -o $@

test/test_av1.out: test/test.c
	java -classpath $(COINS_DIR)/classes coins.driver.Driver \
		-I$(COINS_DIR)/lang/c/include -I$(COINS_DIR)/lang/c/include/samples \
		$< -coins:assembler=as,target=x86_64,lir-opt=$(AV1_LIR_OPT) -o $@

test/test_av2.out: test/test.c
	java -classpath $(COINS_DIR)/classes coins.driver.Driver \
		-I$(COINS_DIR)/lang/c/include -I$(COINS_DIR)/lang/c/include/samples \
		$< -coins:assembler=as,target=x86_64,lir-opt=$(AV2_LIR_OPT) -o $@

test_ssa: test/test_ssa.out
	./$<

test/test_ssa.out: OPT := printflow/prun/my_dce/srd3/divex/printflow
test/test_ssa.out: test/test.c
	java -classpath $(COINS_DIR)/classes coins.driver.Driver \
		-I$(COINS_DIR)/lang/c/include -I$(COINS_DIR)/lang/c/include/samples \
		$< -coins:assembler=as,target=x86_64,lir-opt=$(OPT) -o $@

test_phdce: test/test_phdce.out test/test.log
	./$< | tee test/test_phdce.log
	diff test/test.log test/test_phdce.log

test/test_phdce.out: OPT := divex/printflow/peephole_dce/printflow
test/test_phdce.out: test/test.c
	java -classpath $(COINS_DIR)/classes coins.driver.Driver \
		-I$(COINS_DIR)/lang/c/include -I$(COINS_DIR)/lang/c/include/samples \
		$< -coins:assembler=as,target=x86_64,lir-opt=$(OPT) -o $@

test_phcse: test/test_phcse.out test/test.log
	./$< | tee test/test_phcse.log
	diff test/test.log test/test_phcse.log

test/test_phcse.out: OPT := divex/printflow/peephole_cse/printflow
test/test_phcse.out: test/test.c
	java -classpath $(COINS_DIR)/classes coins.driver.Driver \
		-I$(COINS_DIR)/lang/c/include -I$(COINS_DIR)/lang/c/include/samples \
		$< -coins:assembler=as,target=x86_64,lir-opt=$(OPT) -o $@

test_phcf: test/test_phcf.out test/test.log
	./$< | tee test/test_phcf.log
	diff test/test.log test/test_phcf.log

test/test_phcf.out: OPT := divex/printflow/constant_folding/printflow
test/test_phcf.out: test/test.c
	java -classpath $(COINS_DIR)/classes coins.driver.Driver \
		-I$(COINS_DIR)/lang/c/include -I$(COINS_DIR)/lang/c/include/samples \
		$< -coins:assembler=as,target=x86_64,lir-opt=$(OPT) -o $@


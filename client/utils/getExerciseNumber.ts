export const getExerciseNumber = (exerciseId: string, moduleId: string): number => {
    if (moduleId === 'module-1') {
        const match = exerciseId.match(/module-1-block-(\d+)-task-(\d+)/);
        if (match) {
            const [, blockNum, exNum] = match.map(Number);
            const cumulativeCounts: Record<number, number> = {
                1: 0, 2: 10, 3: 20, 4: 29, 5: 39, 6: 49
            };
            return (cumulativeCounts[blockNum] || 0) + exNum;
        }
        
        const oldMatch = exerciseId.match(/task-(\d+)/);
        if (oldMatch) return parseInt(oldMatch[1]);
    } 
    else if (moduleId === 'module-2') { 
        const match = exerciseId.match(/module-2-block-(\d+)-exercise-(\d+)/);
        if (match) {
            const [, blockNum, exNum] = match.map(Number);
            const cumulativeCounts: Record<number, number> = {
                1: 0, 2: 5, 3: 11, 4: 21, 5: 39,
                6: 51, 7: 59, 8: 74, 9: 80, 10: 90
            };
            return (cumulativeCounts[blockNum] || 0) + exNum;
        }
    } 
    else if (moduleId === 'module-3') {
        const match = exerciseId.match(/module-3-block-(\d+)-exercise-(\d+)/);
        if (match) {
            const [, blockNum, exNum] = match.map(Number);
            const cumulativeCounts: Record<number, number> = {
                1: 0, 2: 11, 3: 24, 4: 31, 5: 43, 6: 57
            };
            return (cumulativeCounts[blockNum] || 0) + exNum;
        }
        
        const num = parseInt(exerciseId);
        if (!isNaN(num) && num >= 101 && num <= 157) {
            return num - 100;
        }
    }
    
    return 1;
};